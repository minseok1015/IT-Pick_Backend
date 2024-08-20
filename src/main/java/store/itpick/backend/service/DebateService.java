package store.itpick.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import store.itpick.backend.common.exception.DebateException;
import store.itpick.backend.common.exception.AuthException;
import store.itpick.backend.common.exception.UserException;
import store.itpick.backend.dto.debate.*;
import store.itpick.backend.dto.vote.PostVoteRequest;
import store.itpick.backend.jwt.JwtProvider;
import store.itpick.backend.model.*;
import store.itpick.backend.repository.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebateService {

    private final KeywordRepository keywordRepository;
    private final DebateRepository debateRepository;
    private final CommentRepository commentRepository;
    private final CommentHeartRepository commentHeartRepository;
    private final UserRepository userRepository;
    private final UserVoteChoiceRepository userVoteChoiceRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteService voteService;
    private final JwtProvider jwtProvider;
    private final S3ImageBucketService s3ImageBucketService;
    private final RecentViewedDebateRepository recentViewedDebateRepository;
    private final TrendDebateRepository trendDebateRepository;

    @Transactional
    public PostDebateResponse createDebate(PostDebateRequest postDebateRequest, long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        Keyword keyword = keywordRepository.findById(postDebateRequest.getKeywordId())
                .orElseThrow(() -> new DebateException(KEYWORD_NOT_FOUND));

        // 이미지 업로드 처리
        String imageUrl = null;
        if (postDebateRequest.getImageFile() != null && !postDebateRequest.getImageFile().isEmpty()) {
            imageUrl = s3ImageBucketService.saveDebateImg(postDebateRequest.getImageFile());
        }

        Debate debate = Debate.builder().title(postDebateRequest.getTitle()).content(postDebateRequest.getContent()).hits(0L).imageUrl(imageUrl).onTrend(false).status("active").createAt(Timestamp.valueOf(LocalDateTime.now())).updateAt(Timestamp.valueOf(LocalDateTime.now())).keyword(keyword).user(user).build();

        debate = debateRepository.save(debate);

        List<VoteOptionRequest> voteOptions = postDebateRequest.getVoteOptions();

        if (voteOptions != null && !voteOptions.isEmpty()) {
            PostVoteRequest postVoteRequest = new PostVoteRequest();
            postVoteRequest.setDebateId(debate.getDebateId());
            postVoteRequest.setMultipleChoice(postDebateRequest.isMultipleChoice());
            voteService.createVote(postVoteRequest, postDebateRequest.getVoteOptions());
        }

        return new PostDebateResponse(debate.getDebateId());
    }

    @Transactional
    public PostCommentResponse createComment(PostCommentRequest postCommentRequest, long userId) {

        Optional<Debate> debateOptional = debateRepository.findById(postCommentRequest.getDebateId());
        if (!debateOptional.isPresent()) {
            throw new DebateException(DEBATE_NOT_FOUND);
        }
        Debate debate = debateOptional.get();

        // 유저 아이디, debateId, comment 내용이 동일한 댓글이 있는지 확인
        List<Comment> existingComment = commentRepository.findByUser_UserIdAndDebate_DebateIdAndComment(userId, postCommentRequest.getDebateId(), postCommentRequest.getComment());
        if (!existingComment.isEmpty()) {
            throw new DebateException(DUPLICATE_COMMENT);
        }

        Comment parentComment = null;
        if (postCommentRequest.getParentCommentId() != null) {
            Optional<Comment> parentCommentOptional = commentRepository.findById(postCommentRequest.getParentCommentId());
            if (parentCommentOptional.isPresent()) {
                parentComment = parentCommentOptional.get();
            } else throw new DebateException(COMMENT_PARENT_NOT_FOUND);
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new AuthException(USER_NOT_FOUND);
        }
        User user = userOptional.get();

        Comment comment = Comment.builder().comment(postCommentRequest.getComment()).debate(debate).parentComment(parentComment).user(user).status("active").createAt(Timestamp.valueOf(LocalDateTime.now())).build();

        commentRepository.save(comment);

        return new PostCommentResponse(comment.getCommentId());
    }

    @Transactional
    public PostCommentHeartResponse creatCommentHeart(PostCommentHeartRequest postCommentHeartRequest, long userId) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new AuthException(USER_NOT_FOUND);
        }

        Optional<Comment> commentOptional = commentRepository.findById(postCommentHeartRequest.getCommentId());
        if (!commentOptional.isPresent()) {
            throw new DebateException(COMMENT_NOT_FOUND);
        }

        CommentHeart existingCommentHeart = commentHeartRepository.findByUserAndComment(userOptional.get(), commentOptional.get());

        if (existingCommentHeart != null) {
            Long deletedCommentHeartId = existingCommentHeart.getCommentHeartId();
            commentHeartRepository.delete(existingCommentHeart);
            return PostCommentHeartResponse.builder()
                    .commentHeartId(deletedCommentHeartId)
                    .build();
        } else {
            // 존재하지 않으면 새로운 CommentHeart 생성 및 저장
            CommentHeart commentHeart = CommentHeart.builder()
                    .user(userOptional.get())
                    .comment(commentOptional.get())
                    .status("active")
                    .createAt(Timestamp.valueOf(LocalDateTime.now()))
                    .build();

            commentHeartRepository.save(commentHeart);
            return new PostCommentHeartResponse(commentHeart.getCommentHeartId());
        }
    }

    @Transactional
    public GetDebateResponse getDebate(Long debateId, long userId) {

        Debate debate = debateRepository.findById(debateId)
                .orElseThrow(() -> new DebateException(DEBATE_NOT_FOUND));

        if(debate.getStatus().equals("deleted")) throw new DebateException(DEBATE_NOT_FOUND);


        // 최근 본 토론 기록 생성 및 저장
        saveRecentViewedDebate(userId, debate);


        debate.setHits(debate.getHits() + 1);
        debateRepository.save(debate);

        User user = debate.getUser();

        boolean userVoted = false;
        String userVoteOptionText = null;

        // 투표가 존재할 경우, 사용자의 투표 여부 확인
        if (debate.getVote() != null) {
            for (VoteOption voteOption : debate.getVote().getVoteOptions()) {
                List<UserVoteChoice> userVoteChoices = userVoteChoiceRepository.findByVoteOptionAndUser(voteOption, user);
                if (!userVoteChoices.isEmpty()) {
                    userVoted = true;
                    userVoteOptionText = userVoteChoices.get(0).getVoteOption().getOptionText();
                    break;
                }
            }
        }

        List<GetDebateResponse.VoteOptionResponse> voteOptions = voteOptionRepository.findByVote(debate.getVote()).stream()
                .map(option -> GetDebateResponse.VoteOptionResponse.builder()
                        .optionText(option.getOptionText())
                        .imgUrl(option.getImgUrl())
                        .voteCount(option.getUserVoteChoices().size())
                        .build())
                .toList();

        List<GetDebateResponse.CommentResponse> comments = commentRepository.findByDebate(debate).stream()
                .map(comment -> {
                    boolean userHearted = commentHeartRepository.existsByCommentAndUser_userId(comment, userId);

                    return GetDebateResponse.CommentResponse.builder()
                            .commentId(comment.getCommentId())
                            .commentText(comment.getComment())
                            .userNickname(comment.getUser().getNickname())
                            .userImgUrl(comment.getUser().getImageUrl())
                            .createAt(comment.getCreateAt())
                            .commentHeartCount(comment.getCommentHearts().size())
                            .userHearted(userHearted)
                            .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null)
                            .build();
                })
                .toList();


        return GetDebateResponse.builder()
                .debateId(debate.getDebateId())
                .debateImgUrl(debate.getImageUrl())
                .multipleChoice(debate.getVote() != null && debate.getVote().isMultipleChoice()) // Vote가 있을 경우만 처리
                .title(debate.getTitle())
                .content(debate.getContent())
                .hits(debate.getHits())
                .onTrend(debate.isOnTrend())
                .status(debate.getStatus())
                .createAt(debate.getCreateAt())
                .updateAt(debate.getUpdateAt())
                .keyword(debate.getKeyword().getKeyword())
                .userNickname(debate.getUser().getNickname())
                .userImgUrl(debate.getUser().getImageUrl())
                .voteOptions(voteOptions)
                .comments(comments)
                .userVoted(userVoted)
                .userVoteOptionText(userVoteOptionText)
                .build();
    }

    @Transactional
    public List<DebateByKeywordDTO> GetDebatesByKeyword(Long keywordID, String sort){
        List<Debate> debates=null;
        if(sort.equals("popularity")){
            debates = debateRepository.findByKeywordIdOrderByHitsDesc(keywordID);
        }
        if (sort.equals("latest")){
            debates = debateRepository.findByKeywordIdOrderByCreateAtDesc(keywordID);
        }
        List<DebateByKeywordDTO> debateList = new ArrayList<>();

        for (Debate debate : debates) {
            if(debate.getStatus().equals("active")){
                String title= debate.getTitle();
                Long debateId =debate.getDebateId();
                String mediaUrl =debate.getImageUrl();
                Long hit = debate.getHits();
                Long comment = (long) debate.getComment().size();
                debateList.add(new DebateByKeywordDTO(title,debateId,mediaUrl,hit,comment));
            }
        }

        return debateList;

    }
    private void saveRecentViewedDebate(Long userId, Debate debate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        RecentViewedDebate recentViewedDebate = new RecentViewedDebate();
        recentViewedDebate.setUser(user);
        recentViewedDebate.setDebate(debate);
        recentViewedDebate.setViewedAt(new Timestamp(System.currentTimeMillis())); // 시청 시간을 저장

        recentViewedDebateRepository.save(recentViewedDebate);
    }

    public List<DebateByKeywordDTO> getRecentViewedDebate(long userId){

        List<RecentViewedDebate> recentViewedDebates = recentViewedDebateRepository.findByUser_UserIdOrderByViewedAtDesc(userId);

        // Debate ID를 통해 Debate 엔티티를 조회
        List<Long> debateIds = recentViewedDebates.stream()
                .map(recentViewedDebate -> recentViewedDebate.getDebate().getDebateId())
                .collect(Collectors.toList());

        List<Debate> debates = debateRepository.findAllById(debateIds);

        // Debate를 DTO로 변환
        return debates.stream()
                .filter(debate -> "active".equals(debate.getStatus()))
                .map(debate -> new DebateByKeywordDTO(debate.getTitle(), debate.getDebateId(), debate.getImageUrl(),debate.getHits(), (long) debate.getComment().size()))
                .collect(Collectors.toList());

    }

    @Transactional
    public void deleteDebate(Long debateId, long userId) {
        Optional<Debate> debate = debateRepository.getDebateByDebateId(debateId);

        if (debate.isEmpty()) {
            throw new DebateException(DEBATE_NOT_FOUND);
        }

        if (!debate.get().getUser().getUserId().equals(userId)) {
            throw new DebateException(INVALID_USER_ID);
        }

        if(debate.get().getStatus().equals("deleted")){
            throw new DebateException(DELETED_DEBATE);
        }


        debateRepository.softDeleteById(debateId);


    }

    @Transactional
    public List<Debate> updateHotDebate() {
        // 현재 시간 및 48시간 전 시간 계산
        Timestamp endTime = new Timestamp(System.currentTimeMillis());
        Timestamp startTime = new Timestamp(endTime.getTime() - 3 * 24 * 60 * 60 * 1000); // 3일 전 시간
        PageRequest pageRequest = PageRequest.of(0, 3); // 상위 3개만 가져오기

        List<TrendDebate> trendDebateList = trendDebateRepository.findAll();
        for(TrendDebate trendDebate : trendDebateList){
            Optional<Debate> updateDebate = debateRepository.getDebateByDebateId(trendDebate.getTrendDebateId());
            if (updateDebate.isPresent()) {
                Debate debate = updateDebate.get();
                debate.setOnTrend(false);
                debateRepository.save(debate);
            }
        }
        // 기존의 TrendDebate 삭제
        trendDebateRepository.deleteAllInBatch();

        // 48시간 동안 조회수가 가장 많이 오른 상위 3개의 Debate 조회
        List<Debate> debateList = debateRepository.findTop3DebatesCreatedInLast3Days(startTime, pageRequest);

        // 새로운 TrendDebate 엔트리 삽입
        for (Debate debate : debateList) {
            TrendDebate trendDebate = TrendDebate.builder()
                    .debate(debate)
                    .updateAt(endTime) // 업데이트된 시간 저장
                    .build();

            debate.setOnTrend(true);
            debateRepository.save(debate);

            trendDebateRepository.save(trendDebate);

        }
        return debateList;

    }

    @Transactional
    public List<DebateByKeywordDTO> getHotDebate() {

        // TrendDebate 테이블에서 현재 저장된 Debate 3개를 가져옵니다.
        List<TrendDebate> trendDebates = trendDebateRepository.findAll();

        // DebateByKeywordDTO 리스트 초기화
        List<DebateByKeywordDTO> debates = new ArrayList<>();

        // TrendDebate에 있는 Debate를 DebateByKeywordDTO로 변환하여 리스트에 추가
        for (TrendDebate trendDebate : trendDebates) {
            Debate debate = trendDebate.getDebate();
            DebateByKeywordDTO debateDTO = new DebateByKeywordDTO(
                    debate.getTitle(),
                    debate.getDebateId(),
                    debate.getImageUrl(),
                    debate.getHits(),
                    (long) debate.getComment().size()
            );
            debates.add(debateDTO);
        }

        // 최종 리스트 반환
        return debates;
    }

}
