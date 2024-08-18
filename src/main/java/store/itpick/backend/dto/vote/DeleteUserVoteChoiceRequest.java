package store.itpick.backend.dto.vote;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteUserVoteChoiceRequest {


    @NotNull(message = "Vote Option ID는 필수입니다.")
    private List<Long> voteOptionIds;
}
