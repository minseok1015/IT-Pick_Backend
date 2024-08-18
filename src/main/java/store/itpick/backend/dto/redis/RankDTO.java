package store.itpick.backend.dto.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class RankDTO {
    private String keyword;
    private long rank;
    private long naverRank;
    private long nateRank;
    private long zumRank;
    private long googleRank;
    private long namuwikiRank;
}
