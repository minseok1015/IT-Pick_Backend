package store.itpick.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store.itpick.backend.common.exception.DebateException;
import store.itpick.backend.common.exception.UserException;
import store.itpick.backend.dto.debate.*;
import store.itpick.backend.dto.vote.PostVoteRequest;
import store.itpick.backend.jwt.JwtProvider;
import store.itpick.backend.model.Comment;
import store.itpick.backend.model.CommentHeart;
import store.itpick.backend.model.Debate;
import store.itpick.backend.model.User;
import store.itpick.backend.repository.CommentHeartRepository;
import store.itpick.backend.repository.CommentRepository;
import store.itpick.backend.repository.DebateRepository;
import store.itpick.backend.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebateService {

    private final DebateRepository debateRepository;
    private final CommentRepository commentRepository;
    private final CommentHeartRepository commentHeartRepository;
    private final UserRepository userRepository;
    private final VoteService voteService;

    @Transactional
    public PostDebateResponse createDebate(PostDebateRequest postDebateRequest) {
        Debate debate = Debate.builder().title(postDebateRequest.getTitle()).content(postDebateRequest.getContent()).hits(0L).onTrend(false).status("active").createAt(Timestamp.valueOf(LocalDateTime.now())).updateAt(Timestamp.valueOf(LocalDateTime.now())).build();

        debate = debateRepository.save(debate);

        List<VoteOptionRequest> voteOptions = postDebateRequest.getVoteOptions();

        if (voteOptions != null && !voteOptions.isEmpty()) {
            PostVoteRequest postVoteRequest = new PostVoteRequest();
            postVoteRequest.setDebateId(debate.getDebateId());
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
            throw new UserException(USER_NOT_FOUND);
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
            throw new UserException(USER_NOT_FOUND);
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
}
