package store.itpick.backend.dto.vote;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUserVoteChoiceRequest {
    @NotNull(message = "User ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "Vote Option ID는 필수입니다.")
    private Long voteOptionId;
}
