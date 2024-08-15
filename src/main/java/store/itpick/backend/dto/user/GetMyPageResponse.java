package store.itpick.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class GetMyPageResponse {

    @AllArgsConstructor
    @Getter
    @Builder
    public static class MyPage{
        private String profileImg;
        private String nickname;
        private String email;
    }

    @AllArgsConstructor
    @Getter
    @Builder
    public static class ProfileEdit {
        private String profileImg;
        private String nickname;
        private String email;
        private String birth_date;
        private List<String> likedTopicList;
    }
}
