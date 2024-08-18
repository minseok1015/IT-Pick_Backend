package store.itpick.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AlarmResponse {
    private String title;
    private String keyword;
    private String duration;
    private Long debateId;
    private boolean isComment;
    private boolean isTrend;

}
