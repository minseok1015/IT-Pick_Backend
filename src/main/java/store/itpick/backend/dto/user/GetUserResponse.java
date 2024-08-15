package store.itpick.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
public class GetUserResponse {

    @AllArgsConstructor
    @Getter
    public static class Nickname{
        private String nickname;
    }

    @AllArgsConstructor
    @Getter
    public static class Email {
        private String email;
    }

    @AllArgsConstructor
    @Getter
    public static class BirthDate {
        private String birth_date;
    }

    @AllArgsConstructor
    @Getter
    public static class LikedTopicList {
        private List<String> likedTopicList;
    }

    @AllArgsConstructor
    @Getter
    public static class ProfileImg {
        private String profileImg;
    }
}
