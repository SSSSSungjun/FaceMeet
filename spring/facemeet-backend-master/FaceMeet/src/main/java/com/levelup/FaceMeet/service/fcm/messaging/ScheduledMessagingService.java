package com.levelup.FaceMeet.service.fcm.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelup.FaceMeet.config.fcm.FcmConstants;
import com.levelup.FaceMeet.config.fcm.FcmProperties;
import com.levelup.FaceMeet.domain.fcm.FcmToken;
import com.levelup.FaceMeet.domain.fcm.FcmTopic;
import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.fcm.*;
import com.levelup.FaceMeet.service.fcm.builder.FcmMessageBuilder;
import com.levelup.FaceMeet.service.fcm.history.NotificationHistoryService;
import com.levelup.FaceMeet.service.fcm.messaging.strategy.MulticastSendStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 예약 메시지 전송 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledMessagingService {

    private final FcmProperties fcmProperties;

    private final FcmTopicRepository fcmTopicRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final ScheduledMessageRepository scheduledMessageRepository;
    private final TopicSubscriptionRepository topicSubscriptionRepository;
    private final UserNotificationHistoryRepository userNotificationHistoryRepository;

    private final MulticastSendStrategy multicastSendStrategy;

    private final ObjectMapper objectMapper;
    private final FcmMessageBuilder fcmMessageBuilder;

    private final NotificationHistoryService notificationHistoryService;

    // 동시성 제어를 위한 처리 중 메시지 추적
    private final Set<String> processingMessages = ConcurrentHashMap.newKeySet();

    /**
     * 사용자 로그인 시 예약 메시지 전송 (비동기)
     */
    @Async("fcmAsyncExecutor")
    public void sendScheduledMessagesToUserAsync(Long userId) {
        try {

            log.info("Thread: {}", Thread.currentThread().getName());
            sendScheduledMessagesToUser(userId);
        } catch (Exception e) {
            log.error("비동기 예약 메시지 전송 실패: userId={}", userId, e);
        }
    }

    /**
     * 사용자에게 예약된 메시지들 전송 (동기)
     */
    public void sendScheduledMessagesToUser(Long userId) {
        try {
            // 토픽 구독 여부 확인
            if (!isSubscribedToDefaultTopic(userId)) {
                log.debug("사용자 {}는 이벤트 토픽을 구독하지 않았습니다.", userId);
                return;
            }

            List<String> tokens = getActiveTokens(userId);
            if (tokens.isEmpty()) {
                log.debug("사용자 {}의 활성 토큰이 없습니다.", userId);
                return;
            }

            // 현재 시간 이후 예약된 메시지들 조회
            List<ScheduledMessage> scheduledMessages = getActiveScheduledMessages();

            // 이미 유저에게 전송된 메시지 필터링
            List<ScheduledMessage> messagesToSend = filterSentMessages(userId, scheduledMessages);

            // 각 메시지 전송
            for (ScheduledMessage message : messagesToSend) {
                sendScheduledMessageSafely(message, userId, tokens);
            }

        } catch (Exception e) {
            // 로그인 프로세스를 방해하지 않도록 예외 삼킴
            log.error("사용자 {}의 예약 메시지 전송 중 오류", userId, e);
        }
    }

    /**
     * 안전한 개별 메시지 전송 (동시성 제어 포함)
     */
    private void sendScheduledMessageSafely(ScheduledMessage message, Long userId, List<String> tokens) {
        String lockKey = userId + "_" + message.getId();

        // 이미 처리 중인 메시지는 스킵
        if (!processingMessages.add(lockKey)) {
            log.debug("이미 처리 중인 메시지 스킵: userId={}, messageId={}", userId, message.getId());
            return;
        }

        try {
            // 먼저 히스토리 생성
            boolean historyCreated = notificationHistoryService.createHistoryIfNotExists(userId, message);

            if (!historyCreated) {
                log.debug("이미 전송된 메시지: userId={}, messageId={}", userId, message.getId());
                return;
            }

            // FCM 전송
            multicastSendStrategy.send(tokens, message.getTitle(), message.getBody(), message.getMessageData());

            log.info("예약 메시지 전송 성공: userId={}, messageId={}", userId, message.getId());

        } catch (Exception e) {
            // FCM 전송 실패 시 히스토리 롤백
            notificationHistoryService.deleteHistory(userId, message.getId());
            log.error("예약 메시지 전송 실패: userId={}, messageId={}", userId, message.getId(), e);

        } finally {
            processingMessages.remove(lockKey);
        }
    }

    /**
     * 이미 전송된 메시지 제외 (배치)
     */
    private List<ScheduledMessage> filterSentMessages(Long userId, List<ScheduledMessage> messages) {
        Set<Long> sentIds = userNotificationHistoryRepository
                .findByUser_IdAndScheduledMessage_IdIn(
                        userId,
                        messages.stream().map(ScheduledMessage::getId).toList()
                )
                .stream()
                .map(history -> history.getScheduledMessage().getId())
                .collect(Collectors.toSet());

        return messages.stream()
                .filter(msg -> !sentIds.contains(msg.getId()))
                .toList();
    }

    /**
     * 예약 메시지 데이터 구성
     */
    private Map<String, String> buildScheduledMessageData(ScheduledMessage message) {
        Map<String, String> data = fcmMessageBuilder.buildScheduledData(message.getTitle(), message.getBody(), message.getMessageData(), message.getSettingId(), message.getScheduledTime());

        try {
            if(message.getMessageData() != null) {
                data.put(FcmConstants.DATA_EVENT_DATA, objectMapper.writeValueAsString(message.getMessageData()));
            }
        } catch (Exception e) {
            log.warn("메시지 데이터 직렬화 실패: {}", e.getMessage());
        }

        return data;
    }

    /**
     * 기본 토픽 구독 여부 확인
     */
    private boolean isSubscribedToDefaultTopic(Long userId) {
        FcmTopic eventTopic = fcmTopicRepository.findByName(fcmProperties.getDefaultTopicName())
                .orElseThrow(() -> new CustomException(ErrorCode.FCM_TOPIC_NOT_FOUND));

        return topicSubscriptionRepository.existsByUserIdAndFcmTopicId(userId, eventTopic.getFcmTopicId());
    }

    /**
     * 사용자의 활성 토큰 조회
     */
    private List<String> getActiveTokens(Long userId) {
        return fcmTokenRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(FcmToken::getDeviceToken)
                .collect(Collectors.toList());
    }

    /**
     * 활성 예약 메시지 조회
     */
    private List<ScheduledMessage> getActiveScheduledMessages() {
        return scheduledMessageRepository.findByStatusAndScheduledTimeAfter(
                ScheduledMessage.MessageStatus.SCHEDULED,
                LocalDateTime.now()
        );
    }
}