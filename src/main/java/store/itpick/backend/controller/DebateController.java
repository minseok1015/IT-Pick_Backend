package store.itpick.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.itpick.backend.common.exception.DebateException;
import store.itpick.backend.common.response.BaseResponse;
import store.itpick.backend.dto.debate.*;
import store.itpick.backend.dto.vote.PostUserVoteChoiceRequest;
import store.itpick.backend.service.DebateService;
import store.itpick.backend.service.VoteService;

import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.*;
import static store.itpick.backend.util.BindingResultUtils.getErrorMessages;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/debate")
public class DebateController {

    private final DebateService debateService;
    private final VoteService voteService;

    @PostMapping("")
    public BaseResponse<PostDebateResponse> createDebate(@Valid @RequestBody PostDebateRequest postDebateRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new DebateException(INVALID_DEBATE_VALUE, getErrorMessages(bindingResult));
        }

        return new BaseResponse<>(debateService.createDebate(postDebateRequest));
    }

    @PostMapping("/comment")
    public BaseResponse<PostCommentResponse> createComment(@Valid @RequestBody PostCommentRequest postCommentRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new DebateException(INVALID_COMMENT_VALUE, getErrorMessages(bindingResult));
        }

        return new BaseResponse<>(debateService.createComment(postCommentRequest));
    }

    @PostMapping("/comment/heart")
    public BaseResponse<PostCommentHeartResponse> creatCommentHeart(@Valid @RequestBody PostCommentHeartRequest postCommentHeartRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new DebateException(INVALID_COMMENT_HEART_VALUE, getErrorMessages(bindingResult));
        }

        return new BaseResponse<>(debateService.creatCommentHeart(postCommentHeartRequest));
    }

    @PostMapping("/vote")
    public BaseResponse<Object> creatUserVoteChoice(@Valid @RequestBody PostUserVoteChoiceRequest postUserVoteChoiceRequest, BindingResult bindingResult) {


        if (bindingResult.hasErrors()) {
            throw new DebateException(INVALID_VOTE_VALUE, getErrorMessages(bindingResult));
        }

        voteService.createUserVoteChoice(postUserVoteChoiceRequest);

        return new BaseResponse<>(HttpStatus.OK);
    }
}
