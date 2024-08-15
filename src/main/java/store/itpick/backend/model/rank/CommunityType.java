package store.itpick.backend.model.rank;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public enum CommunityType {
    TOTAL("total"),
    NAVER("naver"),
    NATE("nate"),
    ZUM("zum"),
    GOOGLE("google"),
    NAMUWIKI("namuwiki");

    private final String communityType;

    public String value() {
        return communityType;
    }

    public static List<CommunityType> getAllExceptTotal() {
        return new ArrayList<>(Arrays.asList(NATE, NAVER, ZUM));
    }
}
