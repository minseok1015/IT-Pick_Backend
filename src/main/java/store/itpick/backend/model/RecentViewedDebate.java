package store.itpick.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;


@Entity
@Table(name = "recent_viewed_debate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentViewedDebate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recent_viewed_debate_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "debate_id", nullable = false)
    private Debate debate;

    @Column(name = "viewed_at", nullable = false)
    private Timestamp viewedAt;
}
