package com.levelup.FaceMeet.service.fcm.messaging;

import com.levelup.FaceMeet.config.fcm.FcmProperties;
import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.event.FcmMessageSentEvent;
import com.levelup.FaceMeet.service.fcm.builder.FcmMessageBuilder;
import com.levelup.FaceMeet.service.fcm.messaging.strategy.MulticastSendStrategy;
import com.levelup.FaceMeet.service.fcm.messaging.strategy.TopicSendStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * FCM 비동기 전송 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmAsyncService {

    private final FcmProperties fcmProperties;

    private final FcmMessageBuilder messageBuilder;
    private final TopicSendStrategy topicSendStrategy;
    private final MulticastSendStrategy multicastSendStrategy;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 사전 알림 비동기 전송
     */
    @Async("fcmAsyncExecutor")
    public void sendPreMessageAsync(ScheduledMessage message) {
        try {
            topicSendStrategy.send(
                    fcmProperties.getDefaultTopicName(),
                    message.getTitle(),
                    message.getBody(),
                    message.getMessageData()
            );

            eventPublisher.publishEvent(
                    FcmMessageSentEvent.success(message.getId(), message.getFcmTopicId())
            );

            log.info("사전 알림 전송 성공: messageId={}, settingId={}", message.getId(), message.getSettingId());

        } catch (Exception e) {
            // 실패 시 상태 업데이트
            eventPublisher.publishEvent(
                    FcmMessageSentEvent.failure(message.getId(), message.getFcmTopicId(), e.getMessage())
            );

            log.error("사전 알림 전송 실패: messageId={}, settingId={}, error={}", message.getId(), message.getSettingId(), e.getMessage());
        }
    }

    /**
     * 채팅 메시지 비동기 전송
     */
    @Async("fcmAsyncExecutor")
    public void sendChatMessageAsync(List<String> tokens, String title, String body, Long roomId, Long userId) {
        try {
            if (tokens.isEmpty()) {
                log.debug("사용자 {}의 활성 토큰이 없어 채팅 알림을 건너뜁니다.", userId);
                return;
            }

            Map<String, String> data = messageBuilder.buildChatData(title, body, roomId);
            multicastSendStrategy.send(tokens, title, body, data);

            log.debug("채팅 알림 전송 완료: userId={}, roomId={}", userId, roomId);

        } catch (Exception e) {
            // 채팅 알림 실패는 치명적이지 않으므로 로그만 남김
            log.error("채팅 알림 전송 실패: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 일반 토픽 메시지 비동기 전송
     */
    @Async("fcmAsyncExecutor")
    public void sendTopicMessageAsync(String topicName, String title, String body, Map<String, String> data) {
        try {
            topicSendStrategy.send(topicName, title, body, data);
            log.info("토픽 메시지 전송 완료: topic={}", topicName);

        } catch (Exception e) {
            log.error("토픽 메시지 전송 실패: topic={}, error={}", topicName, e.getMessage());
        }
    }

    /**
     * 일반 멀티캐스트 메시지 비동기 전송
     */
    @Async("fcmAsyncExecutor")
    public void sendMulticastMessageAsync(List<String> tokens, String title, String body, Map<String, String> data) {
        try {
            multicastSendStrategy.send(tokens, title, body, data);
            log.info("멀티캐스트 메시지 전송 완료: 토큰 수={}", tokens.size());

        } catch (Exception e) {
            log.error("멀티캐스트 메시지 전송 실패: 토큰 수={}, error={}", tokens.size(), e.getMessage());
        }
    }
}