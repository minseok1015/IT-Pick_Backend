package store.itpick.backend.dto.keyword;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SearchDTO {
    private String keyword;
    private long nateRank;
    private long naverRank;
    private long zumRank;
    private long GoogleRank;
    private long NamuwikiRank;
}

