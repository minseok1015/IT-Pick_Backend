package store.itpick.backend.dto.debate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDebateRequest {

    @NotNull(message = "User ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "Debate ID는 필수입니다.")
    private Long debateId;
}