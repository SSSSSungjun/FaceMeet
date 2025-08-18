package com.levelup.FaceMeet.service.fcm;

import com.levelup.FaceMeet.domain.User;
import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.domain.fcm.TopicSubscription;
import com.levelup.FaceMeet.domain.fcm.UserNotificationHistory;
import com.levelup.FaceMeet.dto.FcmMessageDTO.NotificationResponse;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.fcm.ScheduledMessageRepository;
import com.levelup.FaceMeet.repository.fcm.TopicSubscriptionRepository;
import com.levelup.FaceMeet.repository.fcm.UserNotificationHistoryRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationHistoryService {

    private final UserRepository userRepository;
    private final ScheduledMessageRepository scheduledMessageRepository;
    private final TopicSubscriptionRepository topicSubscriptionRepository;
    private final UserNotificationHistoryRepository userNotificationHistoryRepository;

    // 특정 유저에게 메시지 발송 히스토리 생성
    public void createHistoryForUser(Long userId, ScheduledMessage scheduledMessage) {
        User user = userRepository.getReferenceById(userId);

        UserNotificationHistory history = UserNotificationHistory.builder()
                .user(user)
                .scheduledMessage(scheduledMessage)
                .receivedAt(scheduledMessage.getScheduledTime())
                .isRead(false)
                .build();

        userNotificationHistoryRepository.save(history);
    }

    // 토픽 구독자들에게 메시지 발송 히스토리 생성
    public void createHistoryForTopicSubscribers(Long topicId, Long messageId) {
        List<TopicSubscription> subscribers = topicSubscriptionRepository.findByFcmTopicId(topicId);
        ScheduledMessage message = scheduledMessageRepository.getReferenceById(messageId);

        List<UserNotificationHistory> histories = subscribers.stream()
                .map(subscription -> UserNotificationHistory.builder()
                        .user(subscription.getUser())
                        .scheduledMessage(message)
                        .receivedAt(LocalDateTime.now())
                        .isRead(false)
                        .build())
                .collect(Collectors.toList());

        userNotificationHistoryRepository.saveAll(histories);
    }

    public List<NotificationResponse> getNotificationList(Long userId) {
        List<UserNotificationHistory> history = userNotificationHistoryRepository.findAllByUser_IdAndReceivedAtBeforeOrderByScheduledMessage_ScheduledTimeDesc(userId, LocalDateTime.now());

        List<NotificationResponse> list = new ArrayList<>();
        for (UserNotificationHistory h : history) {
            list.add(NotificationResponse.from(h.getScheduledMessage(), h.getIsRead()));
        }

        return list;
    }

    public Long getUnreadCount(Long userId) {
        return userNotificationHistoryRepository.countByUser_IdAndIsReadFalseAndReceivedAtBefore(userId, LocalDateTime.now());
    }

    @Transactional
    public Integer read(Long userId, Long notificationId) {
        int updatedRows = userNotificationHistoryRepository.markAsRead(userId, notificationId);
        if (updatedRows == 0) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "알림을 찾을 수 없거나 이미 처리되었습니다.");
        }
        return updatedRows;
    }
}
