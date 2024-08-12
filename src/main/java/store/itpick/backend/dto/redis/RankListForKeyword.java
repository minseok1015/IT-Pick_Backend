package store.itpick.backend.dto.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RankListForKeyword {
    private long nateRank;
    private long naverRank;
    private long zumRank;
}
