package store.itpick.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import store.itpick.backend.common.argument_resolver.PreAuthorize;
import store.itpick.backend.common.exception.DebateException;
import store.itpick.backend.common.response.BaseResponse;
import store.itpick.backend.dto.debate.*;
import store.itpick.backend.dto.vote.DeleteUserVoteChoiceRequest;
import store.itpick.backend.dto.vote.PostUserVoteChoiceRequest;
import store.itpick.backend.service.AlarmService;
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
    private final AlarmService alarmService;

    @PostMapping("")
    public BaseResponse<PostDebateResponse> createDebate(@PreAuthorize long userId, @Valid @ModelAttribute PostDebateRequest postDebateRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new DebateException(INVALID_DEBATE_VALUE, getErrorMessages(bindingResult));
        }

        return new BaseResponse<>(debateService.createDebate(postDebateRequest, userId));
    }

    @PostMapping("/comment")
    public BaseResponse<PostCommentResponse> createComment(@PreAuthorize long userId, @Valid @RequestBody PostCommentRequest postCommentRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new DebateException(INVALID_COMMENT_VALUE, getErrorMessages(bindingResult));
        }
        PostCommentResponse postCommentResponse = debateService.createComment(postCommentRequest, userId);
        alarmService.createAlarmComment(postCommentResponse.getCommentId(),userId);


        return new BaseResponse<>(debateService.createComment(postCommentRequest, userId));
    }

    @PostMapping("/comment/heart")
    public BaseResponse<PostCommentHeartResponse> creatCommentHeart(@PreAuthorize long userId, @Valid @RequestBody PostCommentHeartRequest postCommentHeartRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new DebateException(INVALID_COMMENT_HEART_VALUE, getErrorMessages(bindingResult));
        }

        return new BaseResponse<>(debateService.creatCommentHeart(postCommentHeartRequest, userId));
    }

    @PostMapping("/vote")
    public BaseResponse<Object> creatUserVoteChoice(@PreAuthorize long userId, @Valid @RequestBody PostUserVoteChoiceRequest postUserVoteChoiceRequest, BindingResult bindingResult) {


        if (bindingResult.hasErrors()) {
            throw new DebateException(INVALID_VOTE_VALUE, getErrorMessages(bindingResult));
        }

        voteService.createUserVoteChoice(postUserVoteChoiceRequest, userId);

        return new BaseResponse<>(HttpStatus.OK);
    }

    @DeleteMapping("/vote")
    public BaseResponse<Object> deleteUserVoteChoice(@PreAuthorize long userId, @Valid @RequestBody DeleteUserVoteChoiceRequest deleteUserVoteChoiceRequest, BindingResult bindingResult){

        if (bindingResult.hasErrors()) {
            throw new DebateException(INVALID_VOTE_DELETE_VALUE, getErrorMessages(bindingResult));
        }

        voteService.deleteUserVoteChoice(deleteUserVoteChoiceRequest, userId);
        return new BaseResponse<>(HttpStatus.OK);
    }

    @GetMapping("/details")
    public BaseResponse<GetDebateResponse> getDebate(
            @RequestParam Long debateId,
            @PreAuthorize long userId) {
        GetDebateResponse debateResponse = debateService.getDebate(debateId, userId);

        return new BaseResponse<>(debateResponse);
    }

    @GetMapping("/keyword")
    public BaseResponse<List<DebateByKeywordDTO>> getDebateByKeyword( @RequestParam Long keywordId,@RequestParam String sort){

        List<DebateByKeywordDTO> debates=debateService.GetDebatesByKeyword(keywordId,sort);

        return new BaseResponse<>(debates);
    }

    @GetMapping("/recent")
    public BaseResponse<List<DebateByKeywordDTO>> getRecentViewedDebate(@PreAuthorize long userId) {
        List<DebateByKeywordDTO> debateResponse = debateService.getRecentViewedDebate(userId);

        return new BaseResponse<>(debateResponse);
    }


    @DeleteMapping("/{debateId}")
    public BaseResponse<?> deleteDebate(@PreAuthorize long userId, @PathVariable Long debateId){
        debateService.deleteDebate(debateId,userId);
        return new BaseResponse<>(null);
    }


    @GetMapping("/trend")
    public BaseResponse<List<DebateByKeywordDTO>> getTrendDebate(){
        List<DebateByKeywordDTO> debateResponse = debateService.updateHotDebate();

        return new BaseResponse<>(debateResponse);
    }
}
