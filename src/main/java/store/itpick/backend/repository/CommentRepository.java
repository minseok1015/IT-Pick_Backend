package store.itpick.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import store.itpick.backend.model.Comment;
import store.itpick.backend.model.Debate;
import store.itpick.backend.model.User;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByDebate(Debate debate);
    @Query("SELECT DISTINCT c.debate FROM Comment c WHERE c.user = :user")
    List<Debate> findDebatesByUserComments(@Param("user") User user);
}
