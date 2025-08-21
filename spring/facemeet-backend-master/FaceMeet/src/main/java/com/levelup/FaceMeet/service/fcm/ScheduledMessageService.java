package com.levelup.FaceMeet.service.fcm;

import com.levelup.FaceMeet.config.fcm.FcmProperties;
import com.levelup.FaceMeet.domain.fcm.FcmTopic;
import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.event.PreMessagePreparedEvent;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.fcm.FcmTopicRepository;
import com.levelup.FaceMeet.repository.fcm.ScheduledMessageRepository;
import com.levelup.FaceMeet.service.fcm.builder.FcmMessageBuilder;
import com.levelup.FaceMeet.service.fcm.history.NotificationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledMessageService {

    private final FcmProperties fcmProperties;
    private final FcmTopicRepository fcmTopicRepository;
    private final ScheduledMessageRepository scheduledMessageRepository;
    private final NotificationHistoryService notificationHistoryService;
    private final FcmMessageBuilder messageBuilder;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 메시지 상태 업데이트
     */
    @Transactional
    public void updateMessageStatus(Long messageId, ScheduledMessage.MessageStatus status) {
        scheduledMessageRepository.findById(messageId)
                .ifPresent(message -> {
                    message.setStatus(status);
                    scheduledMessageRepository.save(message);
                    log.info("메시지 상태 변경: messageId={}, status={}", messageId, status);
                });
    }

    /**
     * 메시지 상태 SENT 로 변경, 유저 수신 히스토리 기록
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatusAndCreateHistory(Long messageId, Long topicId) {
        scheduledMessageRepository.updateStatusById(messageId, ScheduledMessage.MessageStatus.SENT);
        notificationHistoryService.createHistoryForTopicSubscribers(topicId, messageId);
    }

    /**
     * 세팅 생성 시 사전 알림 저장
     */
    @Transactional
    public void preparePreMessage(Long settingId) {

        FcmTopic topic = fcmTopicRepository.findByName(fcmProperties.getDefaultTopicName())
                .orElseThrow(() -> new CustomException(ErrorCode.FCM_TOPIC_NOT_FOUND));

        String title = "매칭권 이벤트 사전 알림";
        String body = String.format("%d분 뒤에 선착순 매칭권 이벤트가 시작됩니다!", fcmProperties.getPreMessageMinutes());

        Map<String, String> data = messageBuilder.buildPreMessageData(title, body, null, settingId);

        // 메시지 기록
        ScheduledMessage message = ScheduledMessage.builder()
                .settingId(settingId)
                .fcmTopicId(topic.getFcmTopicId())
                .scheduledTime(LocalDateTime.now())
                .title(title)
                .body(body)
                .messageData(data)
                .status(ScheduledMessage.MessageStatus.PENDING)
                .build();

        ScheduledMessage savedMessage = scheduledMessageRepository.save(message);

        eventPublisher.publishEvent(new PreMessagePreparedEvent(message.getId()));

        log.info("사전 알림 메시지 생성 완료: settingId={}, messageId={}", settingId, savedMessage.getId());
    }

    @Transactional
    public int markExpiredMessagesAsFailed(LocalDateTime now) {
        // 벌크 업데이트로 성능 개선
        int pendingCount = scheduledMessageRepository.bulkUpdateStatus(
                ScheduledMessage.MessageStatus.PENDING,
                ScheduledMessage.MessageStatus.FAILED,
                now
        );

        int scheduledCount = scheduledMessageRepository.bulkUpdateStatus(
                ScheduledMessage.MessageStatus.SCHEDULED,
                ScheduledMessage.MessageStatus.FAILED,
                now
        );

        if (pendingCount + scheduledCount > 0) {
            log.warn("만료된 메시지 FAILED 처리: PENDING={}, SCHEDULED={}",
                    pendingCount, scheduledCount);
        }

        return pendingCount + scheduledCount;
    }
}
