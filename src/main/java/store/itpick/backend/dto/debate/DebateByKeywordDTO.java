package store.itpick.backend.dto.debate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Builder
@AllArgsConstructor
@Getter
public class DebateByKeywordDTO {
    String title;
    Long debateId;
    String mediaUrl;
    Long hit;
    Long comment;
}

