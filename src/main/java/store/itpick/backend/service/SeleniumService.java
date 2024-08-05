package store.itpick.backend.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store.itpick.backend.model.PeriodType;
import store.itpick.backend.model.Reference;
<<<<<<< HEAD:src/main/java/store/itpick/backend/service/SeleniumService.java
import store.itpick.backend.repository.KeywordRepository;
import store.itpick.backend.service.KeywordService;
import store.itpick.backend.service.ReferenceService;
import store.itpick.backend.util.Redis;
import store.itpick.backend.util.SeleniumUtil;
=======
>>>>>>> parent of 274194d (feat : DTO추가, 데이터베이스 접근하여 keyword, reference 접근):src/main/java/store/itpick/backend/util/Selenium.java

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeleniumService {
    private WebDriver driver;
    private final SeleniumUtil seleniumUtil;

    private final static String WEB_DRIVER_ID = "webdriver.chrome.driver"; // Properties 설정

    @Value("${web-driver.path}")
    private String WEB_DRIVER_PATH;

    private final Redis redis;

    // ChromeDriver 연결 (WEB_DRIVER_PATH 값 주입되고 사용해야 하므로 PostConstruct)
    @PostConstruct
    private void initDriver() {
        seleniumUtil.initDriver();
        driver = seleniumUtil.getDriver();
    }

    public List<Reference> useDriverForZum(String url) {
        driver.get(url);

        Actions actions = new Actions(driver);

        try {
            WebElement btn = driver.findElement(By.className("btn-layer-close-day"));
            actions.click(btn).perform();
        } catch (NoSuchElementException e) {
        }

        WebElement webElement = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("issue_wrap")));
        actions.moveToElement(webElement).perform();

        // 묵시적 대기
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        WebElement issueLayer = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("issue_layer")));

        // 키워드 수집
        List<Reference> references = new ArrayList<>();
        List<WebElement> webElementByKeyword = issueLayer.findElements(By.className("inner_txt"));
        List<String> keywordList = new ArrayList<>();
        for (WebElement element : webElementByKeyword) {
            String keyword = element.getText();

            keywordList.add(keyword);
            System.out.println(keyword);
        }
        redis.saveAll(PeriodType.BY_REAL_TIME, "zum", keywordList);

        // 링크 수집
        List<WebElement> webElementBySearchLink = issueLayer.findElements(By.cssSelector(".link"));
        List<String> linksList = new ArrayList<>();
        for (WebElement element : webElementBySearchLink) {
            String searchLink = element.getAttribute("href");
            linksList.add(searchLink);
            System.out.println(searchLink);
        }

        // 키워드와 링크를 매칭하여 resourceList에 추가
        int size = Math.min(keywordList.size(), linksList.size());
        for (int i = 0; i < size; i++) {
            String keyword = keywordList.get(i);
            String link = linksList.get(i);

            Reference reference = new Reference(keyword, link, "", "", "", "");
            references.add(reference);
        }
        SearchReference(references);

<<<<<<< HEAD:src/main/java/store/itpick/backend/service/SeleniumService.java
        // Reference 객체 검색
        List<Reference> references = SearchReference(keywords, linksList);

        // Reference 객체 저장
        referenceService.saveAll(references);



        // 키워드에 참조 설정 후 저장
        for (int i = 0; i < keywords.size(); i++) {
            Keyword keyword = keywords.get(i);
            Reference reference = references.get(i);
            keyword.setReference(reference); // 참조를 키워드에 설정
        }

        keywordService.saveAll(keywords);
    }


    public void quitDriver() {
        seleniumUtil.quitDriver();
    }



    /**
     나무위키 크롤링
     **/

=======
//        quitDriver();
        return references;
    }

>>>>>>> parent of 274194d (feat : DTO추가, 데이터베이스 접근하여 keyword, reference 접근):src/main/java/store/itpick/backend/util/Selenium.java
    public String useDriverForNamuwiki(String url) {
        driver.get(url);

        Actions actions = new Actions(driver);

        // class명으로 하면 되지만, 계속 변경됨
//        WebElement webElement = driver.findElement(By.className("jM2TE0NV"));
//        actions.moveToElement(webElement).perform();
//        WebElement ul = new WebDriverWait(driver, Duration.ofSeconds(3))
//                .until(ExpectedConditions.visibilityOfElementLocated(By.className("_0SwtPj9H")));
//        System.out.println(ul.getText());

        // xpath
//        WebElement webElement = driver.findElement(By.xpath("/html/body/div/div[1]/div[2]/div/div[6]/div[4]/div"));
//        actions.moveToElement(webElement).perform();
//        WebElement ul = new WebDriverWait(driver, Duration.ofSeconds(3))
//                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div/div[1]/div[2]/div/div[6]/div[4]/div/ul")));
//        System.out.println(ul.getText());

        // until을 통해 준비되는대로 바로 실행
        // 다만, 나무위키의 div 개수가 동적으로 변경되어서 xpath를 어떻게 할지 고민
//        WebElement webElement = new WebDriverWait(driver, Duration.ofSeconds(10))
//                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"app\"]/div[1]/div[2]/div/div[4]/div[2]/div")));
        WebElement webElement = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"app\"]/div[1]/div[2]/div/div[6]/div[4]/div")));
        actions.moveToElement(webElement).perform();

//        WebElement ul = new WebDriverWait(driver, Duration.ofSeconds(10))
//                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"app\"]/div[1]/div[2]/div/div[4]/div[2]/div/ul")));
        WebElement ul = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"app\"]/div[1]/div[2]/div/div[6]/div[4]/div/ul")));
        System.out.println(ul.getText());

        quitDriver();
        return null;
    }

//    public String useDriverForSignal(String url) {
//        driver.get(url);
//
//        WebElement webElement = driver.findElement(By.className("realtime-rank"));
//        System.out.println(webElement.getText());
//
//        quitDriver();
//        return null;
//    }

    public List<Reference> useDriverForNaver(String url) {
        driver.get(url);

        List<WebElement> webElementListByLink = driver.findElements(By.cssSelector(".rank-layer"));
        List<Reference> references = new ArrayList<>();
        List<String> keywordList = new ArrayList<>();

        for (WebElement element : webElementListByLink) {
            String searchLink = element.getAttribute("href");
            WebElement titleElement = element.findElement(By.cssSelector(".rank-text"));
            String keyword = titleElement.getText();
            keywordList.add(keyword);

            Reference reference = new Reference(keyword, searchLink, "", "", "", "");
            references.add(reference);
        }
        redis.saveAll(PeriodType.BY_REAL_TIME, "naver", keywordList);
        SearchReference(references);

//        quitDriver();

        return references;
    }

    public List<Reference> useDriverForMnate(String url) {
        driver.get(url);
        Actions actions = new Actions(driver);

        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("btn_open")));
        actions.click(btn).perform();

        WebElement webElement = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("rankList")));

        // 키워드 수집
        List<Reference> references = new ArrayList<>();
        List<WebElement> webElementByKeyword = webElement.findElements(By.className("kw"));
        List<String> keywordList = new ArrayList<>();
        for (int i = 0; i < webElementByKeyword.size(); i++) {
            WebElement element = webElementByKeyword.get(i);
            String keywords = element.getText();

            // 문자열 맨 앞 1개 문자 제거
            if (!keywords.isEmpty()) {
                keywords = keywords.substring(1);
            }

            // 마지막 루프에서는 맨 앞 2개 문자 제거
            if (i == webElementByKeyword.size() - 1 && keywords.length() > 1) {
                keywords = keywords.substring(1);
            }

            keywordList.add(keywords);
            System.out.println(keywords);
        }
        redis.saveAll(PeriodType.BY_REAL_TIME, "nate", keywordList);

        // 링크 수집
        List<WebElement> webElementBySearchLink = webElement.findElements(By.cssSelector("a"));
        List<String> linksList = new ArrayList<>();
        for (WebElement element : webElementBySearchLink) {
            String searchLink = element.getAttribute("href");
            linksList.add(searchLink);
            System.out.println(searchLink);
        }

        // 키워드와 링크를 매칭하여 resourceList에 추가
        int size = Math.min(keywordList.size(), linksList.size());
        for (int i = 0; i < size; i++) {
            String keyword = keywordList.get(i);
            String link = linksList.get(i);

            Reference reference = new Reference(keyword, link, "", "", "", "");
            references.add(reference);
        }
        SearchReference(references);

//        quitDriver();
        return references;
    }

    private void SearchReference(List<Reference> references) {
        for (Reference reference : references) {
            String documentTitle = reference.getKeywords();
            // Google 뉴스 검색 URL 생성
            String naverSearchUrl = null;
            try {
                naverSearchUrl = "https://search.naver.com/search.naver?where=news&query=" + URLEncoder.encode(documentTitle, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            driver.get(naverSearchUrl);

            // 검색 결과 페이지에서 첫 번째 링크 추출
            WebElement webElement = driver.findElement(By.cssSelector(".news_contents"));

            String newsTitle = webElement.findElement(By.cssSelector(".news_tit")).getText();

            String newsContent = webElement.findElement(By.cssSelector(".news_dsc")).getText();

            String newsLink = webElement.findElement(By.cssSelector(".news_tit")).getAttribute("href");

            String imageUrl = webElement.findElement(By.cssSelector(".thumb")).getAttribute("src");

            reference.setNewsTitle(newsTitle);
            reference.setNewsContent(newsContent);
            reference.setNewsLink(newsLink);
            reference.setImageUrl(imageUrl);
        }
    }

    private void quitDriver() {
        driver.quit(); // webDriver 종료
    }
}
<<<<<<< HEAD:src/main/java/store/itpick/backend/service/SeleniumService.java




=======
>>>>>>> parent of 274194d (feat : DTO추가, 데이터베이스 접근하여 keyword, reference 접근):src/main/java/store/itpick/backend/util/Selenium.java
