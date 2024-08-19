package store.itpick.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.itpick.backend.model.TrendDebate;

@Repository
public interface TrendDebateRepository extends JpaRepository<TrendDebate, Long> {
    void deleteAll();
}
