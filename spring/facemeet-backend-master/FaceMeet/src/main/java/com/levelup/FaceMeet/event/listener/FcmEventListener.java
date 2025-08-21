package com.levelup.FaceMeet.event.listener;

import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.event.FcmMessageSentEvent;
import com.levelup.FaceMeet.event.PreMessagePreparedEvent;
import com.levelup.FaceMeet.repository.fcm.ScheduledMessageRepository;
import com.levelup.FaceMeet.service.fcm.ScheduledMessageService;
import com.levelup.FaceMeet.service.fcm.messaging.FcmAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmEventListener {

    private final ScheduledMessageService scheduledMessageService;
    private final ScheduledMessageRepository scheduledMessageRepository;

    private final FcmAsyncService fcmAsyncService;

    /**
     * FCM 메시지 전송 결과 처리
     * 별도 스레드가 아닌 이벤트 리스너에서 트랜잭션 처리
     */
    @Async("fcmEventExecutor")
    @EventListener
    @Transactional
    public void handleFcmMessageSent(FcmMessageSentEvent event) {
        if (event.isSuccess()) {
            // 메시지 히스토리 생성
            scheduledMessageService.updateStatusAndCreateHistory(
                    event.getMessageId(),
                    event.getTopicId()
            );

            log.info("메시지 전송 완료 처리: messageId = {}", event.getMessageId());
        } else {
            // 상태 FAILED로 변경
            scheduledMessageService.updateMessageStatus(
                    event.getMessageId(),
                    ScheduledMessage.MessageStatus.FAILED
            );

            log.info("메시지 전송 실패 처리: messageId = {}, error = {}", event.getMessageId(), event.getErrorMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePreMessagePrepared(PreMessagePreparedEvent event) {
        ScheduledMessage message = scheduledMessageRepository.findById(event.getMessageId())
                .orElseThrow();
        fcmAsyncService.sendPreMessageAsync(message);
    }
}
