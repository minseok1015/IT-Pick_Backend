package store.itpick.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import store.itpick.backend.common.exception.UserException;
import store.itpick.backend.dto.auth.JwtDTO;
import store.itpick.backend.dto.user.GetMyPageResponse;
import store.itpick.backend.dto.user.GetUserResponse;
import store.itpick.backend.jwt.JwtProvider;
import store.itpick.backend.model.Debate;
import store.itpick.backend.model.LikedTopic;
import store.itpick.backend.model.User;
import store.itpick.backend.repository.DebateRepository;
import store.itpick.backend.repository.LikedTopicRepository;
import store.itpick.backend.repository.UserRepository;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.EMPTY_USER_VALUE;
import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.NULL_USER_VALUE;
import static store.itpick.backend.util.UserUtils.getUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LikedTopicRepository likedTopicRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final DebateRepository debateRepository;


    public void changeNickname(long userId, String nickname) {
        validateIsNull(userId);
        User user = getUser(userId, userRepository);
        user.setNickname(nickname);
        user.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);

    }

    public void changeBirthDate(long userId, String birth_date) {
        User user = getUser(userId, userRepository);
        user.setBirthDate(birth_date);
        user.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);
    }

    public void changeLikedTopics(long userId, List<String> likedTopicList) {
        User user = getUser(userId, userRepository);

        validateIsNull(likedTopicList);
        validateIsEmpty(likedTopicList);


        // 기존 LikedTopic 목록을 맵으로 변환
        Map<String, LikedTopic> existingLikedTopicsMap = user.getLikedTopics()
                .stream()
                .collect(Collectors.toMap(LikedTopic::getLiked_topic, likedTopic -> likedTopic));

        // 모든 기존 LikedTopic의 상태를 Inactive로 설정
        for (LikedTopic likedTopic : existingLikedTopicsMap.values()) {
            likedTopic.setStatus("Inactive");
            likedTopicRepository.save(likedTopic);
        }

        // 새로운 likedTopicIdList를 기준으로 LikedTopic을 업데이트
        for (String sendlikedTopic : likedTopicList) {
            LikedTopic likedTopic = existingLikedTopicsMap.get(sendlikedTopic);
            if (likedTopic != null) {
                // 기존에 있는 likedTopic의 상태를 Active로 변경
                likedTopic.setStatus("Active");
                likedTopic.setUpdateAt(Timestamp.valueOf(LocalDateTime.now()));
                likedTopicRepository.save(likedTopic);
            } else {
                // 새로운 likedTopic을 생성하여 추가
                LikedTopic newLikedTopic = LikedTopic.builder()
                        .status("Active")
                        .createAt(Timestamp.valueOf(LocalDateTime.now()))
                        .user(user)
                        .liked_topic(sendlikedTopic)
                        .build();
                user.getLikedTopics().add(newLikedTopic);
                likedTopicRepository.save(newLikedTopic);
            }
        }
        //TODO 없는 토픽 id 넣을시 Exception throw 추가

        user.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);
    }

    public JwtDTO changeEmail(long userId, String email, String accessToken, String refreshToken) {
        User user = getUser(userId, userRepository);
        user.setEmail(email);
        user.setUpdateAt(new Timestamp(System.currentTimeMillis()));

        String new_accessToken = jwtProvider.createToken_changeEmail(email, userId, accessToken);
        String new_refreshToken = jwtProvider.createToken_changeEmail(email, userId, refreshToken);

        user.setRefreshToken(new_refreshToken);
        userRepository.save(user);


        return new JwtDTO(new_accessToken, new_refreshToken);


    }

    public void changePassword(long userId, String password) {
        validateIsNull(password);
        User user = getUser(userId, userRepository);
        // Encrypt password
        user.setPassword(passwordEncoder.encode(password));
        user.setUpdateAt(new Timestamp(System.currentTimeMillis()));

        userRepository.save(user);
    }

    public void changeProfileImg(long userId, String imgUrl) {
        User user = getUser(userId, userRepository);
        // Encrypt password
        user.setImageUrl(imgUrl);
        user.setUpdateAt(new Timestamp(System.currentTimeMillis()));

        userRepository.save(user);
    }


    // Get
    public GetUserResponse.Nickname getNickname(long userId) {
        User user = getUser(userId, userRepository);
        return new GetUserResponse.Nickname(user.getNickname());
    }

    public GetUserResponse.Email getEmail(long userId) {
        User user = getUser(userId, userRepository);
        return new GetUserResponse.Email(user.getEmail());
    }

    public GetUserResponse.BirthDate getBirthDate(long userId) {
        User user = getUser(userId, userRepository);
        return new GetUserResponse.BirthDate(user.getBirthDate());
    }

    public GetUserResponse.LikedTopicList getLikedTopicList(long userId) {
        User user = getUser(userId, userRepository);
        List<String> existingLikedTopicList = getLikedTopicList(user);

        return new GetUserResponse.LikedTopicList(existingLikedTopicList);
    }

    public GetUserResponse.ProfileImg getProfileImg(long userId) {
        User user = getUser(userId, userRepository);
        return new GetUserResponse.ProfileImg(user.getImageUrl());
    }

    // page


    public GetMyPageResponse.MyPage getMyPage(long userId) {
        User user = getUser(userId, userRepository);
        return GetMyPageResponse.MyPage.builder()
                .profileImg(user.getImageUrl())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .build();
    }


    public GetMyPageResponse.ProfileEdit getProfileEditPage(long userId) {
        User user = getUser(userId, userRepository);
        return GetMyPageResponse.ProfileEdit.builder()
                .profileImg(user.getImageUrl())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .birth_date(user.getBirthDate())
                .likedTopicList(getLikedTopicList(user))
                .build();
    }


    public List<GetMyPageResponse.MyDebate> getMyDebate(long userId) {
        User user = getUser(userId, userRepository);
        List<Debate> myDebateList = debateRepository.getDebateByUser(user);
        List<GetMyPageResponse.MyDebate> myDebateResponseList = new ArrayList<>();

        for (Debate myDebate : myDebateList) {
            myDebateResponseList.add(GetMyPageResponse.MyDebate.builder()
                    .title(myDebate.getTitle())
                    .keyword(myDebate.getKeyword().getKeyword())
                    .duration(getTimeAgo(myDebate.getCreateAt()))
                    .hits(myDebate.getHits())
                    .comments(debateRepository.countCommentsByDebate(myDebate))
                    .build()
            );
        }

        return myDebateResponseList;

    }


    // 관심 주제 반환
    public List<String> getLikedTopicList(User user) {
        return user.getLikedTopics()
                .stream()
                .map(LikedTopic::getLiked_topic) // LikedTopic 객체의 liked_topic 필드를 추출
                .collect(Collectors.toList());  // List<String>으로 수집
    }

    // 몇 분/시간/일 전인지 계산

    public static String getTimeAgo(Timestamp createdAt) {
        // 현재 시각을 LocalDateTime으로 변환
        LocalDateTime now = LocalDateTime.now();

        // Timestamp를 LocalDateTime으로 변환
        LocalDateTime createdDateTime = createdAt.toLocalDateTime();

        // 두 시각 간의 차이를 계산
        Duration duration = Duration.between(createdDateTime, now);

        // 경과된 시간에 따라 결과 문자열 생성
        if (duration.toMinutes() < 1) {
            return "방금전";
        }

        if (duration.toMinutes() < 60) {
            return duration.toMinutes() + "분 전";
        }

        if (duration.toHours() < 24) {
            return duration.toHours() + "시간 전";
        }

        return duration.toDays() + "일 전";
    }


    public String getProfileImgUrl(long userId) {
        User user = getUser(userId, userRepository);
        return user.getImageUrl();
    }

    public void validateIsNull(Object value) {
        if (value == null) {
            throw new UserException(NULL_USER_VALUE);
        }
    }

    public void validateIsEmpty(List<String> value) {
        if (value.isEmpty()) {
            throw new UserException(EMPTY_USER_VALUE);
        }
    }
}
