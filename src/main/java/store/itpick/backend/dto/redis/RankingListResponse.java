package store.itpick.backend.dto.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RankingListResponse {
    private String redisKey;
    private List<RankDTO> rankingList;
}
