package store.itpick.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.itpick.backend.common.exception.ReferenceException;
import store.itpick.backend.dto.rank.RankResponseDTO;
import store.itpick.backend.model.CommunityPeriod;
import store.itpick.backend.model.Keyword;
import store.itpick.backend.model.Reference;
import store.itpick.backend.model.rank.PeriodType;
import store.itpick.backend.repository.CommunityPeriodRepository;
import store.itpick.backend.repository.KeywordRepository;
import store.itpick.backend.util.Redis;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.NO_Search_REFERENCE;


//관련자료 조회하는 Service
@Service
@Slf4j
public class RankService {
    @Autowired
    private KeywordRepository keywordRepository;
    @Autowired
    private CommunityPeriodRepository communityPeriodRepository;
    @Autowired
    private SeleniumService seleniumService;
    @Autowired
    private  KeywordService keywordService;
    @Autowired
    private  Redis redis;


    // 최대 재시도 횟수와 재시도 간격 (초)
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_SECONDS = 5;

    // 재시도 로직을 포함한 함수
    private <T> T executeWithRetries(Callable<T> action, String actionName) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                return action.call(); // 작업 시도
            } catch (TimeoutException e) {
                log.warn("{} 시도 중 TimeoutException 발생, 재시도 중... ({}/{})", actionName, attempt + 1, MAX_RETRIES);
                if (attempt == MAX_RETRIES - 1) {
                    log.error("모든 {} 시도 실패. 종료합니다.", actionName);
                    return null;
                }
                try {
                    TimeUnit.SECONDS.sleep(RETRY_DELAY_SECONDS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("재시도 지연 중 InterruptedException 발생: {}", ie.getMessage());
                    return null;
                }
            } catch (Exception e) {
                log.error("{} 작업 중 예기치 않은 오류 발생: {}", actionName, e.getMessage());
                break;
            }
        }
        return null;
    }

    public RankResponseDTO getReferenceByKeyword(String community, String period, String keyword) {
        List<String> communitiesToCheck = "total".equals(community)
                ? List.of("naver", "zum", "nate", "google", "namuwiki")
                : List.of(community);

        for (String comm : communitiesToCheck) {
            Optional<Keyword> keywordOptional;

            if ("weekly".equals(period)) {
                // weekly 기간에 대한 처리
                keywordOptional = keywordRepository.findTop1ByKeywordAndCommunityOrderByUpdatedAtDesc(keyword, comm);
            } else {
                // 다른 기간에 대한 처리
                Optional<CommunityPeriod> communityPeriodOptional = communityPeriodRepository.findByCommunityAndPeriod(comm, period);

                if (communityPeriodOptional.isPresent()) {
                    CommunityPeriod communityPeriod = communityPeriodOptional.get();
                    keywordOptional = keywordRepository.findTop1ByKeywordAndCommunityOrderByUpdatedAtDesc(keyword, comm);
                } else {
                    continue;
                }
            }

            if (keywordOptional.isPresent()) {
                Keyword latestKeyword = keywordOptional.get();
                Reference reference = latestKeyword.getReference();

                RankResponseDTO response = new RankResponseDTO();
                response.setKeywordId(latestKeyword.getKeywordId());
                response.setKeyword(latestKeyword.getKeyword());
                response.setSearchLink(reference.getSearchLink());
                response.setNewsTitle(reference.getNewsTitle());
                response.setImageUrl(reference.getNewsImage());
                response.setNewsContent(reference.getNewsContent());
                response.setNewsLink(reference.getNewsLink());

                isValidReference(reference.getNewsTitle());

                return response;
            }
        }

        return null;
    }
    private void isValidReference(String newsTitle){
        if(newsTitle.equals("No search")){
            throw new ReferenceException(NO_Search_REFERENCE);
        }
    }

    @Transactional
    public void performHourlyTasks() {
        try {
            seleniumService.initDriver();
            executeWithRetries(() -> seleniumService.useDriverForNaver("https://www.signal.bz/"), "Naver 데이터 수집");
            executeWithRetries(() -> seleniumService.useDriverForMnate("https://m.nate.com/"), "Nate 데이터 수집");
            executeWithRetries(() -> seleniumService.useDriverForZum("https://news.zum.com/"), "Zum 데이터 수집");
            executeWithRetries(() -> seleniumService.useDriverForGoogle("https://trends.google.co.kr/trending/rss?geo=KR"), "Google 데이터 수집");
            executeWithRetries(() -> seleniumService.useDriverForNamuwiki("https://blog.anteater-lab.link/namu-soup/"), "Namuwiki 데이터 수집");

            /** 일간 통합 랭킹 저장 **/
            redis.saveTotalRanking(PeriodType.BY_REAL_TIME);


        } catch (Exception e) {
            log.error("Error during hourly task", e);
        }finally {
            seleniumService.quitDriver();
        }
    }

    // 18시에 실행하는 Daily 작업
    @Transactional
    public void performDailyTasks() {
        log.info("Starting scheduled tasks...performing DailyTask");
        performHourlyTasks(); // 매일 18시에 hourlyTask를 포함

        /**
         * Redis에서 일간 랭킹 생성, 계산
         * **/

        redis.saveDay();
        redis.saveTotalRanking(PeriodType.BY_DAY);

        /** DB에 있는 18시 검색어들을 Daily검색어로 Reference 참조할 수 있도록 함 **/
        keywordService.performDailyTasksNate();
        keywordService.performDailyTasksNaver();
        keywordService.performDailyTasksZum();
        keywordService.performDailyTasksGoogle();
        keywordService.performDailyTasksNamuwiki();
        log.info("Scheduled tasks completed DailyTask.");
    }

    @Transactional
    public void performWeeklyTasks() {
        log.info("Starting weekly task...");
        /**
         * Redis에서 주간 랭킹 계산, 생성
         * **/
        redis.saveWeek();
        redis.saveTotalRanking(PeriodType.BY_WEEK);

        /**
         * DB에서 주간 랭킹 생성, 계산
         * **/


        log.info("Weekly task completed.");
    }


}
