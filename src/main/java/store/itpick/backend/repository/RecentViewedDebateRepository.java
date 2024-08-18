package store.itpick.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.itpick.backend.model.RecentViewedDebate;

import java.util.List;

@Repository
public interface RecentViewedDebateRepository extends JpaRepository<RecentViewedDebate, Long> {
    List<RecentViewedDebate> findByUser_UserIdOrderByViewedAtDesc(Long userId);

}

