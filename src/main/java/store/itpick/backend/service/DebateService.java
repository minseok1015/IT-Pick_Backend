package store.itpick.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store.itpick.backend.common.exception.DebateException;
import store.itpick.backend.common.exception.AuthException;
import store.itpick.backend.common.exception.UserException;
import store.itpick.backend.common.exception.jwt.unauthorized.JwtInvalidTokenException;
import store.itpick.backend.common.exception.jwt.unauthorized.JwtUnauthorizedTokenException;
import store.itpick.backend.dto.debate.*;
import store.itpick.backend.dto.keyword.SearchDTO;
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

    @Transactional
    public PostDebateResponse createDebate(PostDebateRequest postDebateRequest) {

        User user = userRepository.findById(postDebateRequest.getUserId())
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
    public PostCommentResponse createComment(PostCommentRequest postCommentRequest) {

        Optional<Debate> debateOptional = debateRepository.findById(postCommentRequest.getDebateId());
        if (!debateOptional.isPresent()) {
            throw new DebateException(DEBATE_NOT_FOUND);
        }
        Debate debate = debateOptional.get();

        Comment parentComment = null;
        if (postCommentRequest.getParentCommentId() != null) {
            Optional<Comment> parentCommentOptional = commentRepository.findById(postCommentRequest.getParentCommentId());
            if (parentCommentOptional.isPresent()) {
                parentComment = parentCommentOptional.get();
            } else throw new DebateException(COMMENT_PARENT_NOT_FOUND);
        }

        Optional<User> userOptional = userRepository.findById(postCommentRequest.getUserId());
        if (!userOptional.isPresent()) {
            throw new AuthException(USER_NOT_FOUND);
        }
        User user = userOptional.get();

        Comment comment = Comment.builder().comment(postCommentRequest.getComment()).debate(debate).parentComment(parentComment).user(user).status("active").createAt(Timestamp.valueOf(LocalDateTime.now())).build();

        commentRepository.save(comment);

        return new PostCommentResponse(comment.getCommentId());
    }

    @Transactional
    public PostCommentHeartResponse creatCommentHeart(PostCommentHeartRequest postCommentHeartRequest) {

        Optional<User> userOptional = userRepository.findById(postCommentHeartRequest.getUserId());
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
    public GetDebateResponse getDebate(Long debateId, String token) {

        if (jwtProvider.isExpiredToken(token)) {
            throw new JwtUnauthorizedTokenException(INVALID_TOKEN);

        }

        Long userId = jwtProvider.getUserIdFromToken(token);

        Debate debate = debateRepository.findById(debateId)
                .orElseThrow(() -> new DebateException(DEBATE_NOT_FOUND));


        // 최근 본 토론 기록 생성 및 저장
        saveRecentViewedDebate(userId, debate);


        debate.setHits(debate.getHits() + 1);
        debateRepository.save(debate);

        User user = debate.getUser();

        boolean userVoted = false;
        String userVoteOptionText = null;

        for (VoteOption voteOption : debate.getVote().getVoteOptions()) {
            List<UserVoteChoice> userVoteChoices = userVoteChoiceRepository.findByVoteOptionAndUser(voteOption, user);
            if (!userVoteChoices.isEmpty()) {
                userVoted = true;
                // 여러 선택지 중 첫 번째 선택된 옵션의 텍스트를 가져옴 (필요에 따라 조정 가능)
                userVoteOptionText = userVoteChoices.get(0).getVoteOption().getOptionText();
                break;
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
                .multipleChoice(debate.getVote().isMultipleChoice())
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
            String title= debate.getTitle();
            String content =debate.getContent();
            String mediaUrl =debate.getImageUrl();
            Long hit = debate.getHits();
            Long comment = (long) debate.getComment().size();
            debateList.add(new DebateByKeywordDTO(title,content,mediaUrl,hit,comment));
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

    public List<DebateByKeywordDTO> getRecentViewedDebate(String token){
        if (jwtProvider.isExpiredToken(token)) {
            throw new JwtUnauthorizedTokenException(INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserIdFromToken(token);

        List<RecentViewedDebate> recentViewedDebates = recentViewedDebateRepository.findByUser_UserIdOrderByViewedAtDesc(userId);

        // Debate ID를 통해 Debate 엔티티를 조회
        List<Long> debateIds = recentViewedDebates.stream()
                .map(recentViewedDebate -> recentViewedDebate.getDebate().getDebateId())
                .collect(Collectors.toList());

        List<Debate> debates = debateRepository.findAllById(debateIds);

        // Debate를 DTO로 변환
        return debates.stream()
                .map(debate -> new DebateByKeywordDTO(debate.getTitle(), debate.getContent(), debate.getImageUrl(),debate.getHits(), (long) debate.getComment().size()))
                .collect(Collectors.toList());

    }

}
