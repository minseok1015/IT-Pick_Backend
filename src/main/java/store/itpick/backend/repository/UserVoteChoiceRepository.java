package store.itpick.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.itpick.backend.model.User;
import store.itpick.backend.model.UserVoteChoice;
import store.itpick.backend.model.Vote;
import store.itpick.backend.model.VoteOption;

public interface UserVoteChoiceRepository extends JpaRepository<UserVoteChoice, Long> {
    void deleteByVoteOption_VoteAndUser(Vote vote, User user);
    UserVoteChoice findByVoteOptionAndUser(VoteOption voteOption,User user);
}
