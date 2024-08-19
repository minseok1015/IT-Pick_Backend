package store.itpick.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.itpick.backend.model.rank.CommunityType;
import store.itpick.backend.model.rank.PeriodType;
import store.itpick.backend.service.DebateService;
import store.itpick.backend.util.Redis;

import java.util.*;


@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private Redis redis;

    @Autowired
    private DebateService debateService;

    @GetMapping("/*.ico")
    void pathMatch() {
        System.out.println("favicon.ico.");
    }

    @GetMapping("/rank/day")
    public void dayTest() {
        redis.saveDay();
    }

    @GetMapping("/rank/week")
    public void weekTest() {
        redis.saveWeek();
    }

    @GetMapping("/rank/total")
    public void totalTest() {
        redis.saveTotalRanking(PeriodType.BY_REAL_TIME);
        redis.saveTotalRanking(PeriodType.BY_DAY);
        redis.saveTotalRanking(PeriodType.BY_WEEK);
    }

    @GetMapping("/trend")
    public void updateHotDebate(){
        debateService.updateHotDebate();
    }

    @GetMapping("/save-day-manually")
    public String saveDayManually() {
        List<String> dateList = new ArrayList<>(Arrays.asList("240805", "240806", "240807", "240808", "240809", "240810", "240811", "240812", "240813", "240814", "240815"));
        List<CommunityType> communityTypeList = CommunityType.getAllExceptTotal();
        for (CommunityType communityType : communityTypeList) {
            switch (communityType) {
                case NAVER, NATE, ZUM:
                    for (String date : dateList.subList(0, 7)) {
                        if (!RankController.isValidatedDate(PeriodType.BY_DAY, date)) {
                            return "failed";
                        }
                        List<String> keywordList = getKeywordList(communityType, date);
                        redis.saveDay(communityType, date, keywordList);
                        System.out.printf("%s\t%s\t완료\n", communityType.value(), date);
                    }
                    break;
                case GOOGLE, NAMUWIKI:
                    for (String date : dateList) {
                        if (!RankController.isValidatedDate(PeriodType.BY_DAY, date)) {
                            return "failed";
                        }
                        List<String> keywordList = getKeywordList(communityType, date);
                        redis.saveDay(communityType, date, keywordList);
                        System.out.printf("%s\t%s\t완료\n", communityType.value(), date);
                    }
                    break;
            }
        }
        return "success";
    }

    @GetMapping("/save-day-total-manually")
    public String saveDayTotalManually() {
        List<String> dateList = new ArrayList<>(Arrays.asList("240805", "240806", "240807", "240808", "240809", "240810", "240811", "240812", "240813", "240814", "240815", "240816", "240817", "240818"));

        for (String date : dateList) {
            if (!RankController.isValidatedDate(PeriodType.BY_DAY, date)) {
                return "failed";
            }
            redis.saveDayTotalManually(date);
            System.out.printf("total\t%s\t완료\n", date);
        }
        return "success";
    }

    @GetMapping("/save-week-manually")
    public String saveWeekManually() {
        redis.saveWeek();
        redis.saveTotalRanking(PeriodType.BY_WEEK);
        return "success";
    }

    private List<String> getKeywordList(CommunityType communityType, String date) {
        switch (communityType) {
            case NAVER:
                switch (date) {
                    case "240805":
                        return new ArrayList<>(Arrays.asList(
                                "김우진 양궁", "임애지 복싱", "고준희 버닝썬 루머", "지드래곤 저스피스 재단", "올림픽 메달 순위",
                                "사망 폭염 11명", "손흥민 뮌헨", "신형 전술탄도미사일발사대", "코스피 2600선 붕괴", "내년 최저임금 이의제기"));
                    case "240806":
                        return new ArrayList<>(Arrays.asList(
                                "장정윤 김승현", "미용실 먹튀 연예인", "안세영", "신유빈", "인텔",
                                "탁구 단체전 8강", "이스라엘 공격", "황정음 결별", "엔화 강세", "올림픽 메달 순위"));
                    case "240807":
                        return new ArrayList<>(Arrays.asList(
                                "안세영 기자회견 불참", "HBM3E 엔비디아", "진화 함소원", "올림픽 메달 순위", "고교 은사 찌른 20대 감형, 왜?",
                                "서울 아파트 전셋값", "고영욱", "하마스 신와르", "위메프 티몬 대응방안", "쯔양 협박 전국진 축의금"));
                    case "240808":
                        return new ArrayList<>(Arrays.asList(
                                "박태준 16년 만의 금메달", "안세영", "조규성 결장 합병증", "슈가 음주", "조승우 버닝썬 고준희",
                                "올림픽 메달 순위", "휘성", "전현무 역도 박혜정", "카카오 2분기 1340억 원", "뉴욕증시"));
                    case "240809":
                        return new ArrayList<>(Arrays.asList(
                                "방시혁 과즙세연", "구로역서 2명 사망", "김유진", "여자 탁구 동메달", "네이버 2분기 4727억 원",
                                "올림픽 메달 순위", "카라 니콜 허영지", "한지민 최정훈", "민희진 성희롱 어도어", "양지은 부친상 신장"));
                    case "240810":
                        return new ArrayList<>(Arrays.asList(
                                "탁구 동메달", "가족X멜로", "사망 브라질 추락", "리디아 고", "로또1132",
                                "여자 근대 5종 성승민", "북한 쓰레기 풍선", "태권도 이다빈 8강", "윤가이", "김예지 스타"));
                    case "240811":
                        return new ArrayList<>(Arrays.asList(
                                "후보자에 심우정 지명", "박혜정", "정주리 다섯째도 아들", "한동훈 김경수 복권", "전기차 화재 회의",
                                "우상혁", "아이돌 굿즈 제재", "회장 친인척에", "윤가이", "올림픽 메달 순위"));
                }
                break;
            case NATE:
                switch (date) {
                    case "240805":
                        return new ArrayList<>(Arrays.asList(
                                "김우진 양궁", "고준희 버닝썬 루머", "임애지 복싱", "코스피 2600선 붕괴", "올림픽 메달 순위",
                                "사망 폭염 11명", "신형 전술탄도미사일발사대", "손흥민 뮌헨", "내년 최저임금 이의제기", "지드래곤 저스피스 재단"));
                    case "240806":
                        return new ArrayList<>(Arrays.asList(
                                "미용실 먹튀 연예인", "탁구 단체전 8강", "엔화 강세", "올림픽 메달 순위", "장정윤 김승현",
                                "이스라엘 공격", "황정음 결별", "신유빈", "안세영", "인텔"));
                    case "240807":
                        return new ArrayList<>(Arrays.asList(
                                "고영욱", "HBM3E 엔비디아", "진화 함소원", "고교 은사 찌른 20대 감형, 왜?", "위메프 티몬 대응방안",
                                "올림픽 메달 순위", "안세영 기자회견 불참", "서울 아파트 전셋값", "하마스 신와르", "쯔양 협박 전국진 축의금"));
                    case "240808":
                        return new ArrayList<>(Arrays.asList(
                                "전현무 역도 박혜정", "슈가 음주", "안세영", "조승우 버닝썬 고준희", "휘성",
                                "조규성 결장 합병증", "올림픽 메달 순위", "박태준 16년 만의 금메달", "뉴욕증시", "카카오 2분기 1340억 원"));
                    case "240809":
                        return new ArrayList<>(Arrays.asList(
                                "민희진 성희롱 어도어", "김유진", "양지은 부친상 신장", "한지민 최정훈", "올림픽 메달 순위",
                                "구로역서 2명 사망", "여자 탁구 동메달", "방시혁 과즙세연", "네이버 2분기 4727억 원", "카라 니콜 허영지"));
                    case "240810":
                        return new ArrayList<>(Arrays.asList(
                                "사망 브라질 추락", "북한 쓰레기 풍선", "가족X멜로", "신유빈 멀티 메달", "리디아 고",
                                "미녀와 순정남 임수향", "이다빈", "윤가이", "전기차 제조사 공개", "역도"));
                    case "240811":
                        return new ArrayList<>(Arrays.asList(
                                "후보자에 심우정 지명", "정주리 다섯째도 아들", "전기차 화재 회의", "올림픽 금메달 18개", "영상 환불 연예기획사",
                                "황령터널", "한동훈 김경수 복권", "금리 인상", "휘발유 가격 2주", "혁신도서 클러스터"));
                }
                break;
            case ZUM:
                switch (date) {
                    case "240805":
                        return new ArrayList<>(Arrays.asList(
                                "올림픽 메달 순위", "김우진 양궁", "사망 폭염 11명", "임애지 복싱", "고준희 버닝썬 루머",
                                "지드래곤 저스피스 재단", "내년 최저임금 이의제기", "손흥민 뮌헨", "코스피 2600선 붕괴", "신형 전술탄도미사일발사대"));
                    case "240806":
                        return new ArrayList<>(Arrays.asList(
                                "이스라엘 공격", "미용실 먹튀 연예인", "장정윤 김승현", "올림픽 메달 순위", "엔화 강세",
                                "인텔", "신유빈", "안세영", "탁구 단체전 8강", "황정음 결별"));
                    case "240807":
                        return new ArrayList<>(Arrays.asList(
                                "고교 은사 찌른 20대 감형, 왜?", "고영욱", "하마스 신와르", "쯔양 협박 전국진 축의금", "올림픽 메달 순위",
                                "진화 함소원", "HBM3E 엔비디아", "안세영 기자회견 불참", "위메프 티몬 대응방안", "서울 아파트 전셋값"));
                    case "240808":
                        return new ArrayList<>(Arrays.asList(
                                "전현무 역도 박혜정", "조규성 결장 합병증", "휘성", "박태준 16년 만의 금메달", "슈가 음주",
                                "카카오 2분기 1340억 원", "조승우 버닝썬 고준희", "뉴욕증시", "올림픽 메달 순위", "안세영"));
                    case "240809":
                        return new ArrayList<>(Arrays.asList(
                                "올림픽 메달 순위", "카라 니콜 허영지", "방시혁 과즙세연", "여자 탁구 동메달", "한지민 최정훈",
                                "양지은 부친상 신장", "김유진", "구로역서 2명 사망", "민희진 성희롱 어도어", "네이버 2분기 4727억 원"));
                    case "240810":
                        return new ArrayList<>(Arrays.asList(
                                "경영권 이정재", "지진희 눈물의 여왕", "김예지 스타", "태권도 이다빈 8강", "김판곤 감독 데뷔전",
                                "오피셜 3주", "올림픽 메달 순위", "임영웅", "복싱 칼리프", "진지희 신유빈"));
                    case "240811":
                        return new ArrayList<>(Arrays.asList(
                                "탁구 동메달", "정주리 오형제", "임영웅", "해리스 트럼프", "전동스쿠터 경찰",
                                "톨라 마라톤", "쓰레기 풍선 240여개", "엔하이픈 재팬", "한동훈 김경수 복권", "젤렌스키 침략자"));
                }
                break;
            case GOOGLE:
                switch (date) {
                    case "240805":
                        return new ArrayList<>(Arrays.asList(
                                "손흥민 뮌헨", "신형 전술탄도미사일발사대", "고준희 버닝썬 루머", "임애지 복싱", "지드래곤 저스피스 재단",
                                "사망 폭염 11명", "올림픽 메달 순위", "코스피 2600선 붕괴", "내년 최저임금 이의제기", "김우진 양궁"));
                    case "240806":
                        return new ArrayList<>(Arrays.asList(
                                "인텔", "엔화 강세", "신유빈", "안세영", "올림픽 메달 순위",
                                "이스라엘 공격", "황정음 결별", "장정윤 김승현", "탁구 단체전 8강", "미용실 먹튀 연예인"));
                    case "240807":
                        return new ArrayList<>(Arrays.asList(
                                "진화 함소원", "HBM3E 엔비디아", "안세영 기자회견 불참", "올림픽 메달 순위", "쯔양 협박 전국진 축의금",
                                "위메프 티몬 대응방안", "하마스 신와르", "고교 은사 찌른 20대 감형, 왜?", "서울 아파트 전셋값", "고영욱"));
                    case "240808":
                        return new ArrayList<>(Arrays.asList(
                                "안세영", "박태준 16년 만의 금메달", "전현무 역도 박혜정", "조승우 버닝썬 고준희", "카카오 2분기 1340억 원",
                                "뉴욕증시", "조규성 결장 합병증", "휘성", "슈가 음주", "올림픽 메달 순위"));
                    case "240809":
                        return new ArrayList<>(Arrays.asList(
                                "방시혁 과즙세연", "네이버 2분기 4727억 원", "민희진 성희롱 어도어", "카라 니콜 허영지", "구로역서 2명 사망",
                                "여자 탁구 동메달", "양지은 부친상 신장", "한지민 최정훈", "올림픽 메달 순위", "김유진"));
                    case "240810":
                        return new ArrayList<>(Arrays.asList(
                                "사망 브라질 추락", "북한 쓰레기 풍선", "미녀와 순정남 차화연", "가족X멜로", "리디아 고",
                                "미녀와 순정남 임수향", "1132회 로또 1등", "승리 인도네시아 버닝썬", "전기차 제조사 공개", "LG 3연승 질주"));
                    case "240811":
                        return new ArrayList<>(Arrays.asList(
                                "김경수 복권 반대", "청소년 이복현", "사망 브라질 추락", "전현무 캐스터 역도", "잠실 장미아파트 신통기획",
                                "아이돌 굿즈 제재", "손흥민 토트넘", "회장 친인척에", "윤가이", "설영우 즈베즈다"));
                    case "240812":
                        return new ArrayList<>(Arrays.asList(
                                "36주 낙태 의사", "양준혁", "조지호 신임 경찰청장", "혐의 유아인 출석", "안세영 배드민턴",
                                "공수처 尹대통령 확보", "유어 아너 김명민", "대통령 이명박 만찬", "박혜정 역도 코치진", "세븐틴 불참 정한은"));
                    case "240813":
                        return new ArrayList<>(Arrays.asList(
                                "사기 범죄 무기징역까지", "광복회 광복절 기념식에", "용인시 언남동 데이터센터", "쌍둥이 임신 유산", "민희진 성희롱 신고에",
                                "10대 여학생 우울증", "하마스 텔아비브에 로켓", "방탄소년단 10억 멜론", "7월 생산자물가 0.1%", "잠실 롯데 두산전"));
                    case "240814":
                        return new ArrayList<>(Arrays.asList(
                                "세계문화유산 선릉 훼손", "청라 전기차 화재", "헌법재판관 김복형 윤승은", "박지윤 이혼 최동석", "최주환 끝내기 홈런",
                                "복서 머스크 조앤롤링", "해단식 대한체육회 선수단", "7월 소비자물가 2.9%", "서울경찰청장 경찰청 이호영", "서정희 프러포즈 김태현"));
                    case "240815":
                        return new ArrayList<>(Arrays.asList(
                                "홍명보호 박건하 김진규", "살인자 발언에 살인자인가", "장나라 이혼 굿파트너", "돌싱글즈2 이다은 광복절", "소매판매 예상치 상회",
                                "장영란 22억 빚", "日언론 광복절 언급", "딸 앞에서 얼굴", "수지맞은 우리", "푸틴 우크라"));

                }
                break;
            case NAMUWIKI:
                switch (date) {
                    case "240805":
                        return new ArrayList<>(Arrays.asList(
                                "올림픽 메달 순위", "임애지 복싱", "김우진 양궁", "신형 전술탄도미사일발사대", "지드래곤 저스피스 재단",
                                "손흥민 뮌헨", "사망 폭염 11명", "코스피 2600선 붕괴", "고준희 버닝썬 루머", "내년 최저임금 이의제기"));
                    case "240806":
                        return new ArrayList<>(Arrays.asList(
                                "엔화 강세", "올림픽 메달 순위", "신유빈", "장정윤 김승현", "미용실 먹튀 연예인",
                                "인텔", "이스라엘 공격", "안세영", "황정음 결별", "탁구 단체전 8강"));
                    case "240807":
                        return new ArrayList<>(Arrays.asList(
                                "HBM3E 엔비디아", "고영욱", "안세영 기자회견 불참", "진화 함소원", "하마스 신와르",
                                "위메프 티몬 대응방안", "고교 은사 찌른 20대 감형, 왜?", "올림픽 메달 순위", "쯔양 협박 전국진 축의금", "서울 아파트 전셋값"));
                    case "240808":
                        return new ArrayList<>(Arrays.asList(
                                "휘성", "안세영", "카카오 2분기 1340억 원", "올림픽 메달 순위", "조규성 결장 합병증",
                                "조승우 버닝썬 고준희", "뉴욕증시", "슈가 음주", "전현무 역도 박혜정", "박태준 16년 만의 금메달"));
                    case "240809":
                        return new ArrayList<>(Arrays.asList(
                                "방시혁 과즙세연", "양지은 부친상 신장", "민희진 성희롱 어도어", "한지민 최정훈", "구로역서 2명 사망",
                                "올림픽 메달 순위", "여자 탁구 동메달", "네이버 2분기 4727억 원", "카라 니콜 허영지", "김유진"));
                    case "240810":
                        return new ArrayList<>(Arrays.asList(
                                "경영권 이정재", "지진희 눈물의 여왕", "김예지 스타", "사격 박하준", "김판곤 감독 데뷔전",
                                "오피셜 3주", "서건우 오혜리 코치", "임영웅", "배드민턴 안세영", "근대 5종 전웅태"));
                    case "240811":
                        return new ArrayList<>(Arrays.asList(
                                "가족X멜로", "박혜정", "광복절 경축식 불참", "신유빈 멀티 메달", "영상 환불 연예기획사",
                                "황령터널", "엔하이픈 록인", "권익위 간부", "8명 중 가족돌봄", "역도"));
                    case "240812":
                        return new ArrayList<>(Arrays.asList(
                                "별똥별", "유어 아너", "양준혁", "티아라 송치", "임영웅",
                                "유성우", "이주호 의대생", "김경수 복권", "안세영 배드민턴", "전현무 역도 박혜정"));
                    case "240813":
                        return new ArrayList<>(Arrays.asList(
                                "한지민 최정훈", "정글밥", "유어 아너", "박수홍 김다예", "난카이 대지진",
                                "PPI", "임영웅", "밀양 가해자", "사도광산 조태열", "양준혁"));
                    case "240814":
                        return new ArrayList<>(Arrays.asList(
                                "오상욱", "이주명", "나는 솔로", "이주명 띠동갑", "김병철 윤세아",
                                "안세영 선배 청소", "완벽한 가족", "서울대생 스티커", "롤링 머스크 고소", "임영웅"));
                    case "240815":
                        return new ArrayList<>(Arrays.asList(
                                "완벽한 가족", "뉴라이트", "유혜정", "트럼프 해리스", "난카이 대지진",
                                "살인자 전현희", "의대생 집회", "레알마드리드", "임영웅", "수지맞은 우리"));
                }
                break;
        }
        return null;
    }
}
