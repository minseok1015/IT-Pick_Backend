package store.itpick.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store.itpick.backend.common.exception.AuthException;
import store.itpick.backend.common.exception.DebateException;
import store.itpick.backend.common.exception.UserException;
import store.itpick.backend.common.exception.jwt.unauthorized.JwtUnauthorizedTokenException;
import store.itpick.backend.dto.debate.*;
import store.itpick.backend.dto.user.AlarmResponse;
import store.itpick.backend.dto.vote.PostVoteRequest;
import store.itpick.backend.jwt.JwtProvider;
import store.itpick.backend.model.*;
import store.itpick.backend.repository.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.*;
import static store.itpick.backend.util.DateUtils.getTimeAgo;
import static store.itpick.backend.util.UserUtils.getUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AlarmRepository alarmRepository;
    private final DebateRepository debateRepository;


    @Transactional
    public void createAlarmComment(long commentId, long userId) {
        User user = getUser(userId, userRepository);
        Comment comment = commentRepository.findById(commentId).orElse(null);

        if (comment.getParentComment() != null) {
            Optional<Comment> parentCommentOptional = commentRepository.findById(comment.getParentComment().getCommentId());
            if (parentCommentOptional.isPresent()
                    && parentCommentOptional.get().getUser().getUserId() != userId) {
                // Step 1: 알람의 수를 확인
                Long alarmCount = alarmRepository.countByUser(user);

                // Step 2: 알람이 30개 이상이면 오래된 알람 삭제
                if (alarmCount >= 30) {
                    List<Alarm> alarms = alarmRepository.findByUserOrderByCreateAtAsc(user);
                    Alarm oldestAlarm = alarms.get(0); // 가장 오래된 알람
                    alarmRepository.delete(oldestAlarm);
                }

                Alarm alarm = Alarm.builder()
                        .comment(comment)
                        .user(parentCommentOptional.get().getUser())
                        .createAt(Timestamp.valueOf(LocalDateTime.now()))
                        .build();

                alarmRepository.save(alarm);
            } else throw new DebateException(COMMENT_PARENT_NOT_FOUND);
        }
    }


    @Transactional
    public void createAlarmTrend(List<Debate> trendDebateList) {
        for (Debate trendDebate : trendDebateList) {

            User user = trendDebate.getUser();
            // Step 1: 알람의 수를 확인
            Long alarmCount = alarmRepository.countByUser(user);

            // Step 2: 알람이 30개 이상이면 오래된 알람 삭제
            if (alarmCount >= 30) {
                List<Alarm> alarms = alarmRepository.findByUserOrderByCreateAtAsc(user);
                Alarm oldestAlarm = alarms.get(0); // 가장 오래된 알람
                alarmRepository.delete(oldestAlarm);
            }
            //
            Alarm alarm = Alarm.builder()
                    .debate(trendDebate)
                    .user(user)
                    .createAt(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            alarmRepository.save(alarm);
        }
    }


    @Transactional
    public List<AlarmResponse> getAlarms(long userId) {
        User user = getUser(userId, userRepository);
        List<Alarm> alarms = alarmRepository.findByUserOrderByCreateAtAsc(user);
        List<AlarmResponse> alarmResponseList = new ArrayList<>();

        for(Alarm alarm : alarms) {

            if (alarm.getDebate() == null){
                Debate debate = alarm.getComment().getDebate();
                alarmResponseList.add(AlarmResponse.builder()
                        .title(debate.getTitle())
                        .keyword(debate.getKeyword().getKeyword())
                        .duration(getTimeAgo(alarm.getCreateAt()))
                        .debateId(debate.getDebateId())
                        .isComment(true)
                        .isTrend(false)
                        .build());
                continue;
            }
            Debate debate = alarm.getDebate();
            alarmResponseList.add(AlarmResponse.builder()
                    .title(debate.getTitle())
                    .keyword(debate.getKeyword().getKeyword())
                    .duration(getTimeAgo(alarm.getCreateAt()))
                    .debateId(debate.getDebateId())
                    .isComment(false)
                    .isTrend(true)
                    .build());
        }
        return  alarmResponseList;
    }



}
