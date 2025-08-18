package com.levelup.FaceMeet.service.fcm;

import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.repository.fcm.ScheduledMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleRecoveryService {

    private final ScheduledMessageRepository scheduledMessageRepository;
    private final MessageSchedulerService messageSchedulerService;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverSchedules() {
        LocalDateTime now = LocalDateTime.now();

        // 미래 시간의 PENDING 메시지들 - 스케줄 복구
        List<ScheduledMessage> pendingMessages = scheduledMessageRepository
                .findByStatusAndScheduledTimeAfter(ScheduledMessage.MessageStatus.PENDING, now);

        for (ScheduledMessage message : pendingMessages) {
            messageSchedulerService.scheduleAllTasks(message.getSettingId(), message);
        }

        log.info("스케줄 복구 완료: {}개", pendingMessages.size());

        // 과거 시간의 PENDING/SCHEDULED 메시지들 - FAILED로 변경
        List<ScheduledMessage> expiredPendingMessages = scheduledMessageRepository
                .findByStatusAndScheduledTimeBefore(ScheduledMessage.MessageStatus.PENDING, now);

        List<ScheduledMessage> expiredScheduledMessages = scheduledMessageRepository
                .findByStatusAndScheduledTimeBefore(ScheduledMessage.MessageStatus.SCHEDULED, now);

        int failedCount = 0;

        // PENDING 상태의 만료된 메시지들 처리
        for (ScheduledMessage message : expiredPendingMessages) {
            message.setStatus(ScheduledMessage.MessageStatus.FAILED);
            scheduledMessageRepository.save(message);
            failedCount++;
            log.warn("만료된 PENDING 메시지를 FAILED로 변경: messageId={}, scheduledTime={}",
                    message.getId(), message.getScheduledTime());
        }

        // SCHEDULED 상태의 만료된 메시지들 처리
        for (ScheduledMessage message : expiredScheduledMessages) {
            message.setStatus(ScheduledMessage.MessageStatus.FAILED);
            scheduledMessageRepository.save(message);
            failedCount++;
            log.warn("만료된 SCHEDULED 메시지를 FAILED로 변경: messageId={}, scheduledTime={}",
                    message.getId(), message.getScheduledTime());
        }

        log.info("만료된 메시지 FAILED 처리 완료: {}개", failedCount);
    }
}