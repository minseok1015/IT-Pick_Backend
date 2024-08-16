package store.itpick.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import store.itpick.backend.model.Debate;

import java.util.List;

@Repository
public interface DebateRepository extends JpaRepository<Debate, Long> {
    @Query("SELECT d FROM Debate d WHERE d.keyword.keywordId = :keywordId ORDER BY d.hits DESC")
    List<Debate> findByKeywordIdOrderByHitsDesc(Long keywordId);


    @Query("SELECT d FROM Debate d WHERE d.keyword.keywordId = :keywordId ORDER BY d.createAt DESC")
    List<Debate> findByKeywordIdOrderByCreateAtDesc(Long keywordId);
}