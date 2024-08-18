package store.itpick.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.itpick.backend.model.Alarm;
import store.itpick.backend.model.Comment;
import store.itpick.backend.model.CommentHeart;
import store.itpick.backend.model.User;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    // 사용자별로 오래된 알람을 가져오기 위한 메서드
    List<Alarm> findByUserOrderByCreateAtAsc(User user);

    // 특정 사용자의 알람 수를 세는 메서드
    Long countByUser(User user);

}