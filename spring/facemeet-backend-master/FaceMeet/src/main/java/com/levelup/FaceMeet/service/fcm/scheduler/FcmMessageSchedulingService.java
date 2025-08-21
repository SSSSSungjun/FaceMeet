package com.levelup.FaceMeet.service.fcm.scheduler;

import com.levelup.FaceMeet.config.fcm.FcmProperties;
import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.repository.fcm.ScheduledMessageRepository;
import com.levelup.FaceMeet.service.fcm.ScheduledMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 메시지 스케줄링 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmMessageSchedulingService {

    private final TaskScheduler fcmTaskScheduler;
    private final ScheduledMessageRepository scheduledMessageRepository;
    private final ScheduledMessageService scheduledMessageService;
    private final FcmProperties fcmProperties;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 36000000) // 10시간마다
    public void cleanupCompletedTasks() {
        synchronized (scheduledTasks) {
            scheduledTasks.entrySet().removeIf(entry ->
                    entry.getValue().isDone() || entry.getValue().isCancelled()
            );
        }
    }

    /**
     * 세팅에 대한 모든 스케줄 작업 설정
     */
    @Transactional
    public void scheduleAllTasks(Long settingId, ScheduledMessage message) {
        LocalDateTime startTime = message.getScheduledTime();

        // 사전 알림 스케줄링
        schedulePreMessage(
                settingId,
                startTime.minusMinutes(fcmProperties.getPreMessageMinutes())
        );

        // 메시지 상태 변경 스케줄링
        scheduleStatusUpdate(
                message.getId(),
                startTime.minusMinutes(fcmProperties.getMessageDispatchMinutes()),
                ScheduledMessage.MessageStatus.SCHEDULED
        );

        scheduleStatusUpdate(
                message.getId(),
                startTime,
                ScheduledMessage.MessageStatus.SENT
        );

        log.info("메시지 스케줄링 완료: settingId={}, messageId={}, time={}",
                settingId, message.getId(), startTime);
    }

    /**
     * 세팅 관련 모든 스케줄 취소
     */
    @Transactional(readOnly = true)
    public void cancelAllTasksForSetting(Long settingId) {
        String preMessageKey = "pre_" + settingId;
        cancelTask(preMessageKey);

        scheduledMessageRepository.findBySettingId(settingId).forEach(message -> {
            cancelTask("scheduled_" + message.getId());
            cancelTask("sent_" + message.getId());
        });

        log.info("세팅 {} 관련 모든 스케줄 취소 완료", settingId);
    }



    private void schedulePreMessage(Long settingId, LocalDateTime executionTime) {
        String taskKey = "pre_" + settingId;
        scheduleTask(taskKey, executionTime, () -> scheduledMessageService.preparePreMessage(settingId));
    }

    private void scheduleStatusUpdate(Long messageId, LocalDateTime executionTime,
                                      ScheduledMessage.MessageStatus status) {
        String taskKey = status == ScheduledMessage.MessageStatus.SCHEDULED
                ? "scheduled_" + messageId
                : "sent_" + messageId;

        scheduleTask(taskKey, executionTime, () -> scheduledMessageService.updateMessageStatus(messageId, status));
    }



    private void scheduleTask(String taskKey, LocalDateTime executionTime, Runnable task) {
        synchronized (scheduledTasks) {
            cancelTask(taskKey);

            if (executionTime.isAfter(LocalDateTime.now())) {
                Date triggerTime = Date.from(executionTime.atZone(ZoneId.systemDefault()).toInstant());
                ScheduledFuture<?> future = fcmTaskScheduler.schedule(
                        wrapTaskWithCleanup(taskKey, task),
                        triggerTime
                );
                scheduledTasks.put(taskKey, future);
                log.debug("태스크 스케줄링: key={}, time={}", taskKey, executionTime);
            } else {
                task.run();
                log.debug("태스크 즉시 실행: key={}", taskKey);
            }
        }
    }

    private void cancelTask(String taskKey) {
        synchronized (scheduledTasks) {
            ScheduledFuture<?> future = scheduledTasks.get(taskKey);
            if (future != null && !future.isDone()) {
                future.cancel(false);
                scheduledTasks.remove(taskKey);
                log.debug("태스크 취소: key={}", taskKey);
            }
        }
    }

    private Runnable wrapTaskWithCleanup(String taskKey, Runnable task) {
        return () -> {
            try {
                task.run();
            } finally {
                scheduledTasks.remove(taskKey);
            }
        };
    }
}