package store.itpick.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.itpick.backend.model.Comment;
import store.itpick.backend.model.CommentHeart;
import store.itpick.backend.model.User;

import java.util.Optional;

@Repository
public interface CommentHeartRepository extends JpaRepository<CommentHeart, Long> {
    CommentHeart findByUserAndComment(User user, Comment comment);
    boolean existsByCommentAndUser_userId(Comment comment, Long userId);
}