package store.itpick.backend.model;


import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "trend_debate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendDebate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trend_debate_id")
    private Long trendDebateId;

    @OneToOne
    @JoinColumn(name = "debate_id", nullable = false)
    private Debate debate;

    @Column(name = "update_at", nullable = false)
    private Timestamp updateAt;
}
