package store.itpick.backend.controller;

import org.openqa.selenium.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import store.itpick.backend.common.exception.ReferenceException;
import store.itpick.backend.common.response.BaseErrorResponse;
import store.itpick.backend.common.response.BaseResponse;
import store.itpick.backend.common.response.status.ResponseStatus;
import store.itpick.backend.dto.redis.MainKeywordResponse;
import store.itpick.backend.model.rank.CommunityType;
import store.itpick.backend.model.rank.PeriodType;
import store.itpick.backend.dto.rank.RankResponseDTO;
import store.itpick.backend.model.Reference;
import store.itpick.backend.service.*;
import store.itpick.backend.util.Redis;
import store.itpick.backend.service.RankService;
import store.itpick.backend.service.SeleniumService;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.*;

@RestController
@RequestMapping("/rank")
public class RankController {


    private final RankService rankService;

    @Autowired
    public RankController(RankService rankService) {
        this.rankService = rankService;
    }

    @Autowired
    private SeleniumService seleniumService;

    @Autowired
    private Redis redis;

    @Autowired
    private KeywordService keywordService;


    // 최대 재시도 횟수와 재시도 간격 (초)
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_SECONDS = 5;

    // 재시도 로직을 포함한 함수
    private <T> T executeWithRetries(Callable<T> action, String actionName) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                return action.call(); // 작업 시도
            } catch (TimeoutException e) {
                System.out.println(actionName + " 시도 중 TimeoutException 발생, 재시도 중... (" + (attempt + 1) + "/" + MAX_RETRIES + ")");
                if (attempt == MAX_RETRIES - 1) {
                    System.out.println("모든 " + actionName + " 시도 실패. 종료합니다.");
                    return null;
                }
                try {
                    TimeUnit.SECONDS.sleep(RETRY_DELAY_SECONDS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            } catch (Exception e) {
                System.out.println(actionName + " 작업 중 예기치 않은 오류 발생: " + e.getMessage());
                break;
            }
        }
        return null;
    }

    @GetMapping("/zum")
    public List<Reference> getRankFromZum() {
        String url = "https://news.zum.com/";
        return executeWithRetries(() -> seleniumService.useDriverForZum(url), "Zum 데이터 수집");
    }

    @GetMapping("/namuwiki")
    public String getRankFromNamuwiki() {
        seleniumService.initDriver();
        String url = "https://blog.anteater-lab.link/namu-soup/";
        return executeWithRetries(() -> seleniumService.useDriverForNamuwiki(url), "Namuwiki 데이터 수집");
    }

    @GetMapping("/reference")
    public BaseResponse<RankResponseDTO> getReference(
            @RequestParam String community,
            @RequestParam String period,
            @RequestParam String keyword) {

        RankResponseDTO rankResponse = rankService.getReferenceByKeyword(community, period, keyword);

        if (rankResponse == null) {
            throw new ReferenceException(EMPTY_REFERENCE);
        }

        return new BaseResponse<>(rankResponse);
    }

    @GetMapping("/update/naver")
    public void getUpdate() {
        keywordService.performDailyTasksNaver();
    }

    @GetMapping("/update/nate")
    public void updateNate() {
        keywordService.performDailyTasksNate();
    }

    @GetMapping("/update/zum")
    public void updateZum() {
        keywordService.performDailyTasksZum();
    }

    @GetMapping("/update/hourly")
    public void updateHourly() {
        rankService.performHourlyTasks();
    }

    @GetMapping("/update/daily")
    public void updateDaily() {
        rankService.performDailyTasks();
    }

    @GetMapping("/update/weekly")
    public void updateWeekly() {
        rankService.performWeeklyTasks();
    }

    @GetMapping("/naver")
    public List<Reference> getRankFromSignal() {
        String url = "https://www.signal.bz/";
        return executeWithRetries(() -> seleniumService.useDriverForNaver(url), "Signal 데이터 수집");
    }

    @GetMapping("/mnate")
    public List<Reference> getRankFromMnate() {
        String url = "https://m.nate.com/";
        return executeWithRetries(() -> seleniumService.useDriverForMnate(url), "Mnate 데이터 수집");
    }

    @GetMapping("/google")
    public List<Reference> getRankFromGoogle() {
        String url = "https://trends.google.co.kr/trending/rss?geo=KR";
        return executeWithRetries(() -> seleniumService.useDriverForGoogle(url), "Google 데이터 수집");
    }

    @GetMapping("")
//    public BaseResponse<GetRankingListResponse> getRankingList(@RequestParam String community, @RequestParam String period) {
    public ResponseStatus getRankingList(@RequestParam String community, @RequestParam String period, @RequestParam String date) {
        CommunityType communityType = getCommunityType(community);
        PeriodType periodType = getPeriodType(period);
        if (communityType == null || periodType == null || !isValidatedDate(periodType, date)) {
            return new BaseErrorResponse(BAD_REQUEST);
        }
        return new BaseResponse<>(redis.getRankingList(communityType, periodType, date));
    }

    @GetMapping("/badge")
    public ResponseStatus getRankingBadgeForKeyword(@RequestParam String keyword, @RequestParam String period, @RequestParam String date) {
        PeriodType periodType = getPeriodType(period);
        if (periodType == null || !isValidatedDate(periodType, date)) {
            return new BaseErrorResponse(BAD_REQUEST);
        }
        return new BaseResponse<>(redis.getRankingBadgeResponse(keyword, periodType, date));
    }

    @GetMapping("/main-keywords")
    public BaseResponse<MainKeywordResponse> getMainKeywords() {
        return new BaseResponse<>(redis.getMainKeywords());
    }

//    private static boolean isValidatedDate(PeriodType periodType, String date) {
    public static boolean isValidatedDate(PeriodType periodType, String date) {
        if (periodType == PeriodType.BY_REAL_TIME) {    // 무조건 통과
            return true;
        }
        if (date.matches("[0-9]{6}")) {
            return true;
        }
        return false;
    }

//    private static CommunityType getCommunityType(String community) {
    public static CommunityType getCommunityType(String community) {
        return switch (community) {
            case "naver" -> CommunityType.NAVER;
            case "nate" -> CommunityType.NATE;
            case "zum" -> CommunityType.ZUM;
            case "google" -> CommunityType.GOOGLE;
            case "namuwiki" -> CommunityType.NAMUWIKI;
            case "total" -> CommunityType.TOTAL;
            default -> null;
        };
    }

//    private static PeriodType getPeriodType(String period) {
    public static PeriodType getPeriodType(String period) {
        return switch (period) {
            case "real_time" -> PeriodType.BY_REAL_TIME;
            case "day" -> PeriodType.BY_DAY;
            case "week" -> PeriodType.BY_WEEK;
            default -> null;
        };
    }
}
