package store.itpick.backend.dto.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class RankListForKeyword {
    private long nateRank;
    private long naverRank;
    private long zumRank;
    private long googleRank;
    private long namuwikiRank;
}
