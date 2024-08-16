package store.itpick.backend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import store.itpick.backend.dto.redis.RankListForKeyword;
import store.itpick.backend.dto.redis.GetRankingListResponse;
import store.itpick.backend.dto.redis.RankDTO;
import store.itpick.backend.model.rank.CommunityType;
import store.itpick.backend.model.rank.PeriodType;
import store.itpick.backend.model.rank.RankingWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class Redis {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public String saveRealtime(CommunityType communityType, PeriodType periodType, List<String> keywordList) {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        String key = makeKey(communityType, periodType, "not needed");

        if (keywordList.size() >= 10) {
            redisTemplate.delete(key);
        }

        for (int i = 0; i < 10; i++) {
            int score = 10 - i;
            System.out.printf("key: %s\tscore: %d\tkeyword: %s\n", key, score, keywordList.get(i));
            zSetOperations.add(key, keywordList.get(i), score);
        }
//        redisTemplate.expire(key, Duration.ofSeconds(20));

        return key;
    }

    public void saveDay() {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();

        LocalDate localDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyMMdd");
        List<String> realTimeKeyList = getKeyList(PeriodType.BY_REAL_TIME, "not needed");
        List<String> dayKeyList = getKeyList(PeriodType.BY_DAY, localDate.format(dateTimeFormatter));

        for (int i = 0; i < realTimeKeyList.size(); i++) {
            int score = 10;
            redisTemplate.delete(dayKeyList.get(i));    // 기존 키 삭제
            for (Object realTimeKeyword : zSetOperations.reverseRange(realTimeKeyList.get(i), 0, 9)) {
                zSetOperations.add(dayKeyList.get(i), realTimeKeyword, score--);
            }
        }
    }

    public void saveWeek() {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();

        LocalDate mondayOfPreviousWeek = DateUtils.getMondayOfPreviousWeek();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyMMdd");
        List<List<String>> dayKeyListOfPreviousWeek = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate dayOfPreviousWeek = mondayOfPreviousWeek.plusDays(i);
            List<String> dayKeyList = getKeyList(PeriodType.BY_DAY, dayOfPreviousWeek.format(dateTimeFormatter));
            dayKeyListOfPreviousWeek.add(dayKeyList);
        }
        List<String> weekKeyList = getKeyList(PeriodType.BY_WEEK, mondayOfPreviousWeek.format(dateTimeFormatter));

        for (List<String> dayKeyList : dayKeyListOfPreviousWeek) {   // 지난주 월요일부터 일요일까지, 각 커뮤니티의 키 리스트
            for (int i = 0; i < dayKeyList.size(); i++) {  // naver, nate, zum, google, namuwiki에 대하여
                int score = 10;
                redisTemplate.delete(weekKeyList.get(i));   // 기존 키 삭제
                for (Object dayKeyword : zSetOperations.reverseRange(dayKeyList.get(i), 0, 9)) {
                    if (!Boolean.TRUE.equals(zSetOperations.addIfAbsent(weekKeyList.get(i), dayKeyword, score))) {
                        zSetOperations.add(weekKeyList.get(i), dayKeyword, score + zSetOperations.score(weekKeyList.get(i), dayKeyword));
                    }
                    score--;
                }
            }
        }
    }

    public void saveTotalRanking(PeriodType periodType) {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        String date = switch (periodType) {
            case BY_REAL_TIME -> "not needed";
            case BY_DAY -> DateUtils.localDateToString(LocalDate.now());
            case BY_WEEK -> DateUtils.localDateToString(DateUtils.getMondayOfPreviousWeek());
        };
        String totalKey = makeKey(CommunityType.TOTAL, periodType, date);
        List<String> keyList = getKeyList(periodType, date); // key for naver, nate, zum, google, namuwiki
        for (String key : keyList) {
            int weight = getWeight(key);
            int rank = 1;
            for (Object keyword : zSetOperations.reverseRange(key, 0, 9)) {
                int score = (11 - rank) * weight;
                if (!Boolean.TRUE.equals(zSetOperations.addIfAbsent(totalKey, keyword, score))) {
                    zSetOperations.add(totalKey, keyword, score + zSetOperations.score(totalKey, keyword));
                }
                rank++;
            }
        }
    }

    public GetRankingListResponse getRankingList(CommunityType communityType, PeriodType periodType, String date) {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        String key = makeKey(communityType, periodType, date);

        List<RankDTO> rankingList = new ArrayList<>();
        long rank = 1;
        for (Object object : zSetOperations.reverseRange(key, 0, 9)) {
            String keyword = (String) object;
            RankListForKeyword rankingBadgeResponse = getRankingBadgeResponse(keyword, periodType, date);
//            rankingList.add(new RankDTO(keyword, rank++, rankingBadgeResponse.getNateRank(), rankingBadgeResponse.getNaverRank(), rankingBadgeResponse.getZumRank()));
            rankingList.add(RankDTO.builder()
                    .keyword(keyword)
                    .rank(rank++)
                    .naverRank(rankingBadgeResponse.getNaverRank())
                    .nateRank(rankingBadgeResponse.getNateRank())
                    .zumRank(rankingBadgeResponse.getZumRank())
                    .googleRank(rankingBadgeResponse.getGoogleRank())
                    .namuwikiRank(rankingBadgeResponse.getNamuwikiRank())
                    .build());
        }

        return new GetRankingListResponse(key, rankingList);
    }

    public RankListForKeyword getRankingBadgeResponse(String keyword, PeriodType periodType, String date) {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        List<CommunityType> communityTypeList = CommunityType.getAllExceptTotal();
        List<Long> rankByCommunity = new ArrayList<>();

        for (CommunityType communityType : communityTypeList) {
            String key = makeKey(communityType, periodType, date);
            Long rank = zSetOperations.reverseRank(key, keyword);
            if (rank == null) {
                rankByCommunity.add((long) -1);
                continue;
            }
            rankByCommunity.add(rank + 1);
        }
        return RankListForKeyword.builder()
                .naverRank(rankByCommunity.get(0))
                .nateRank(rankByCommunity.get(1))
                .zumRank(rankByCommunity.get(2))
                .googleRank(rankByCommunity.get(3))
                .namuwikiRank(rankByCommunity.get(4))
                .build();
    }

    private static String makeKey(CommunityType communityType, PeriodType periodType, String date) {
        String key = communityType.value() + "_";
        switch (periodType) {
            case BY_REAL_TIME -> key += periodType.get();
            case BY_DAY -> key += DateUtils.getDate(DateUtils.getLocalDate(date));
            case BY_WEEK -> key += DateUtils.getWeek(DateUtils.getLocalDate(date));
        }
        return key;
    }

    private static List<String> getKeyList(PeriodType periodType, String date) {
        List<String> keyList = new ArrayList<>();
        for (CommunityType communityType : CommunityType.getAllExceptTotal()) {
            keyList.add(makeKey(communityType, periodType, date));
        }
        return keyList;
    }

    private static int getWeight(String key) {
        if (key.startsWith(CommunityType.NAVER.value())) {
            return RankingWeight.NAVER.get();
        }
        if (key.startsWith(CommunityType.NATE.value())) {
            return RankingWeight.NATE.get();
        }
        if (key.startsWith(CommunityType.ZUM.value())) {
            return RankingWeight.ZUM.get();
        }
        if (key.startsWith(CommunityType.GOOGLE.value())) {
            return RankingWeight.GOOGLE.get();
        }
        if (key.startsWith(CommunityType.NAMUWIKI.value())) {
            return RankingWeight.NAMUWIKI.get();
        }
        return -1;
    }
}
