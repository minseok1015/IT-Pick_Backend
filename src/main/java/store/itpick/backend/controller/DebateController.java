package store.itpick.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import store.itpick.backend.common.exception.DebateException;
import store.itpick.backend.common.response.BaseResponse;
import store.itpick.backend.dto.debate.*;
import store.itpick.backend.dto.vote.DeleteUserVoteChoiceRequest;
import store.itpick.backend.dto.vote.PostUserVoteChoiceRequest;
import store.itpick.backend.service.DebateService;
import store.itpick.backend.service.VoteService;

import java.util.List;

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

    @DeleteMapping("/vote")
    public BaseResponse<Object> deleteUserVoteChoice(@Valid @RequestBody DeleteUserVoteChoiceRequest deleteUserVoteChoiceRequest, BindingResult bindingResult){

        if (bindingResult.hasErrors()) {
            throw new DebateException(INVALID_VOTE_DELETE_VALUE, getErrorMessages(bindingResult));
        }

        voteService.deleteUserVoteChoice(deleteUserVoteChoiceRequest);
        return new BaseResponse<>(HttpStatus.OK);
    }

    @GetMapping("/details")
    public BaseResponse<GetDebateResponse> getDebate(
            @RequestParam Long debateId,
            @RequestHeader("Authorization") String token,BindingResult bindingResult) {

        String jwtToken = token.substring(7);


        if (bindingResult.hasErrors()) {
            throw new DebateException(INVALID_GET_DEBATE_VALUE, getErrorMessages(bindingResult));
        }

        GetDebateResponse debateResponse = debateService.getDebate(debateId, jwtToken);

        return new BaseResponse<>(debateResponse);
    }

    @GetMapping("/keyword")
    public BaseResponse<List<DebateByKeywordDTO>> getDebateByKeyword( @RequestParam Long keywordId,@RequestParam String sort){

        List<DebateByKeywordDTO> debates=debateService.GetDebatesByKeyword(keywordId,sort);

        return new BaseResponse<>(debates);
    }
}
