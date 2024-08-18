package store.itpick.backend.dto.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class MainKeywordResponse {
    private String naverMainKeyword;
    private String nateMainKeyword;
    private String zumMainKeyword;
    private String googleMainKeyword;
    private String namuwikiMainKeyword;
}
