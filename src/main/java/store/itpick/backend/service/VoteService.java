package store.itpick.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store.itpick.backend.common.exception.UserException;
import store.itpick.backend.common.exception.VoteException;
import store.itpick.backend.dto.debate.VoteOptionRequest;
import store.itpick.backend.dto.vote.DeleteUserVoteChoiceRequest;
import store.itpick.backend.dto.vote.PostUserVoteChoiceRequest;
import store.itpick.backend.dto.vote.PostVoteRequest;
import store.itpick.backend.dto.vote.PostVoteResponse;
import store.itpick.backend.model.*;
import store.itpick.backend.repository.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteService {

    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final DebateRepository debateRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final UserVoteChoiceRepository userVoteChoiceRepository;
    private final S3ImageBucketService s3ImageBucketService;

    @Transactional
    public PostVoteResponse createVote(PostVoteRequest postVoteRequest, List<VoteOptionRequest> voteOptions) {

        Debate debate = debateRepository.findById(postVoteRequest.getDebateId())
                .orElseThrow(() -> new VoteException(INVALID_DEBATE_ID));

        Vote vote = Vote.builder()
                .status("active")
                .createAt(Timestamp.valueOf(LocalDateTime.now()))
                .updateAt(Timestamp.valueOf(LocalDateTime.now()))
                .debate(debate)
                .multipleChoice(postVoteRequest.isMultipleChoice())
                .build();

        vote = voteRepository.save(vote);
        if (voteOptions != null && !voteOptions.isEmpty()) {
            createVoteOptions(vote, voteOptions);
        }

        return new PostVoteResponse(vote.getVoteId());
    }

    @Transactional
    public void createVoteOptions(Vote vote, List<VoteOptionRequest> voteOptions) {
        for (VoteOptionRequest option : voteOptions) {
            String imgUrl = null;

            if (option.getImageFile() != null && !option.getImageFile().isEmpty()) {
                imgUrl = s3ImageBucketService.saveDebateImg(option.getImageFile());
            }

            VoteOption voteOption = VoteOption.builder()
                    .optionText(option.getOptionText())
                    .imgUrl(imgUrl)
                    .status("active")
                    .createAt(Timestamp.valueOf(LocalDateTime.now()))
                    .updateAt(Timestamp.valueOf(LocalDateTime.now()))
                    .vote(vote)
                    .build();

            voteOptionRepository.save(voteOption);
        }
    }

    @Transactional
    public void createUserVoteChoice(PostUserVoteChoiceRequest postUserVoteChoiceRequest, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        List<VoteOption> voteOptions = voteOptionRepository.findAllById(postUserVoteChoiceRequest.getVoteOptionIds());

        if (voteOptions.isEmpty()) {
            throw new VoteException(VOTE_OPTION_NOT_FOUND);
        }

        Vote vote = voteOptions.get(0).getVote();

        if (!vote.isMultipleChoice() && postUserVoteChoiceRequest.getVoteOptionIds().size() > 1) {
            throw new VoteException(MULTIPLE_SELECTION_NOT_ALLOWED);
        }

         userVoteChoiceRepository.deleteByVoteOption_VoteAndUser(vote, user);


        // 새로운 선택 저장
        for (VoteOption voteOption : voteOptions) {
            UserVoteChoice userVoteChoice = UserVoteChoice.builder()
                    .user(user)
                    .voteOption(voteOption)
                    .status("active")
                    .createAt(Timestamp.valueOf(LocalDateTime.now()))
                    .updateAt(Timestamp.valueOf(LocalDateTime.now()))
                    .build();

            userVoteChoiceRepository.save(userVoteChoice);
        }
    }


    @Transactional
    public void deleteUserVoteChoice(DeleteUserVoteChoiceRequest deleteUserVoteChoiceRequest, long userId){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        List<Long> voteOptionIds = deleteUserVoteChoiceRequest.getVoteOptionIds();

        for (Long voteOptionId : voteOptionIds) {
            VoteOption voteOption = voteOptionRepository.findById(voteOptionId)
                    .orElseThrow(() -> new VoteException(VOTE_OPTION_NOT_FOUND));

            List<UserVoteChoice> userVoteChoices = userVoteChoiceRepository.findByVoteOptionAndUser(voteOption, user);

            if (!userVoteChoices.isEmpty()) {
                for (UserVoteChoice userVoteChoice : userVoteChoices) {
                    userVoteChoiceRepository.delete(userVoteChoice);
                }
            } else {
                throw new VoteException(USER_VOTE_CHOICE_NOT_FOUND);
            }
        }
    }

}
