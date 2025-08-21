package com.levelup.FaceMeet.service.fcm.history;

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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationHistoryService {

    private final UserRepository userRepository;
    private final ScheduledMessageRepository scheduledMessageRepository;
    private final TopicSubscriptionRepository topicSubscriptionRepository;
    private final UserNotificationHistoryRepository userNotificationHistoryRepository;

    /**
     * 특정 사용자에게 메시지 발송 히스토리 생성
     */
    @Transactional
    public void createHistoryForUser(Long userId, ScheduledMessage message) {
        User user = userRepository.getReferenceById(userId);

        UserNotificationHistory history = buildHistory(user, message, message.getScheduledTime());
        userNotificationHistoryRepository.save(history);

        log.debug("알림 히스토리 생성: userId={}, messageId={}", user.getId(), message.getId());
    }

    /**
     * 토픽 구독자들에게 메시지 발송 히스토리 생성
     */
    @Transactional
    public void createHistoryForTopicSubscribers(Long topicId, Long messageId) {
        List<TopicSubscription> subscribers = topicSubscriptionRepository.findByFcmTopicId(topicId);

        if (subscribers.isEmpty()) {
            log.warn("토픽 {}의 구독자가 없습니다.", topicId);
            return;
        }

        ScheduledMessage message = scheduledMessageRepository.getReferenceById(messageId);
        LocalDateTime now = LocalDateTime.now();

        List<UserNotificationHistory> histories = subscribers.stream()
                .map(sub -> buildHistory(sub.getUser(), message, now))
                .collect(Collectors.toList());

        userNotificationHistoryRepository.saveAll(histories);
        log.info("토픽 {} 알림 전송 내역 기록 완료", topicId);
    }

    /**
     * 사용자의 알림 목록 조회
     */
    public List<NotificationResponse> getUserNotificationList(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        return userNotificationHistoryRepository.findAllByUser_IdAndReceivedAtBeforeOrderByScheduledMessage_ScheduledTimeDesc(userId, now)
                .stream()
                .map(history -> NotificationResponse.from(
                        history.getScheduledMessage(),
                        history.getIsRead()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    public Long getUnreadCount(Long userId) {
        return userNotificationHistoryRepository.countByUser_IdAndIsReadFalseAndReceivedAtBefore(userId, LocalDateTime.now());
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        int updatedRows = userNotificationHistoryRepository.markAsRead(userId, notificationId);

        if (updatedRows == 0) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "알림을 찾을 수 없거나 이미 처리되었습니다.");
        }

        log.debug("알림 읽음 처리: userId={}, notificationId={}", userId, notificationId);
    }

    private UserNotificationHistory buildHistory(User user, ScheduledMessage message, LocalDateTime receivedAt) {
        return UserNotificationHistory.builder()
                .user(user)
                .scheduledMessage(message)
                .receivedAt(receivedAt)
                .isRead(false)
                .build();
    }

    /**
     * 히스토리 생성 (중복 방지)
     */
    @Transactional
    public boolean createHistoryIfNotExists(Long userId, ScheduledMessage message) {
        try {
            // 이미 존재하는지 확인
            boolean exists = userNotificationHistoryRepository.existsByUser_IdAndScheduledMessage_Id(userId, message.getId());

            if (exists) {
                return false;
            }

            createHistoryForUser(userId, message);
            return true;

        } catch (DataIntegrityViolationException e) {
            log.debug("알림 수신 내역 이미 존재: userId={}, messageId={}", userId, message.getId());
            return false;
        }
    }

    /**
     * 히스토리 제거
     */
    @Transactional
    public void deleteHistory(Long userId, Long messageId) {
        try {
            userNotificationHistoryRepository.deleteByUser_IdAndScheduledMessage_Id(userId, messageId);
            log.debug("알림 수신 내역 제거: userId={}, messageId={}", userId, messageId);
        } catch (Exception e) {
            log.error("알림 수신 내역 제거 실패: userId={}, messageId={}", userId, messageId, e);
        }
    }
}
