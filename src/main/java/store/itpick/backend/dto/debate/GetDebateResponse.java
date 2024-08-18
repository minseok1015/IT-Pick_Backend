package store.itpick.backend.dto.debate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetDebateResponse {

    private Long debateId;
    private String title;
    private String content;
    private Long hits;
    private boolean onTrend;
    private String status;
    private Timestamp createAt;
    private Timestamp updateAt;
    private String keyword;
    private String userNickname;
    private String userImgUrl;
    private List<VoteOptionResponse> voteOptions;
    private List<CommentResponse> comments;
    private boolean userVoted;
    private String userVoteOptionText;
    private String debateImgUrl;
    private boolean multipleChoice;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VoteOptionResponse {
        private String optionText;
        private String imgUrl;
        private long voteCount;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentResponse {
        private Long commentId;
        private String commentText;
        private String userNickname;
        private String userImgUrl;
        private Timestamp createAt;
        private long commentHeartCount;
        private boolean userHearted;
        private Long parentCommentId;
    }
}
