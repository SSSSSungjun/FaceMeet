package com.levelup.FaceMeet.service.fcm.scheduler;

import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.repository.fcm.ScheduledMessageRepository;
import com.levelup.FaceMeet.service.fcm.ScheduledMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleRecoveryService {

    private final ScheduledMessageRepository scheduledMessageRepository;
    private final FcmMessageSchedulingService fcmMessageSchedulingService;
    private final ScheduledMessageService scheduledMessageService;

    private static final int BATCH_SIZE = 100;

    @Async("recoveryExecutor")  // 전용 executor 사용
    @EventListener(ApplicationReadyEvent.class)
    public void recoverSchedules() {
        try {
            log.info("스케줄 복구 작업 시작");

            LocalDateTime now = LocalDateTime.now();

            // 미래 메시지 복구 (배치 처리)
            int recoveredCount = recoverPendingMessages(now);

            // 만료 메시지 처리 (벌크 업데이트)
            int failedCount = scheduledMessageService.markExpiredMessagesAsFailed(now);

            log.info("스케줄 복구 완료 - 복구: {}개, 실패처리: {}개",
                    recoveredCount, failedCount);

        } catch (Exception e) {
            log.error("스케줄 복구 중 오류 발생", e);
            // 복구 실패해도 애플리케이션은 정상 구동
        }
    }

    private int recoverPendingMessages(LocalDateTime now) {
        int recoveredCount = 0;

        // 페이징으로 메모리 효율성 개선
        int page = 0;
        List<ScheduledMessage> batch;

        do {
            batch = scheduledMessageRepository.findByStatusAndScheduledTimeAfter(
                    ScheduledMessage.MessageStatus.PENDING,
                    now);

            for (ScheduledMessage message : batch) {
                try {
                    fcmMessageSchedulingService.scheduleAllTasks(
                            message.getSettingId(), message
                    );
                    recoveredCount++;

                    // 부하 분산
                    Thread.sleep(10);

                } catch (Exception e) {
                    log.error("메시지 복구 실패: messageId={}",
                            message.getId(), e);
                    // 개별 실패가 전체를 중단시키지 않음
                }
            }
        } while (!batch.isEmpty());

        return recoveredCount;
    }
}