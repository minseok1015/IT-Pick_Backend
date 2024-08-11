package store.itpick.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "user_vote_choise")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVoteChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_vote_choise_id")
    private Long UserVoteChoiseId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "create_at", nullable = false)
    private Timestamp createAt;

    @Column(name = "update_at")
    private Timestamp updateAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name="vote_option_id",nullable = false)
    private VoteOption voteOption;
}


