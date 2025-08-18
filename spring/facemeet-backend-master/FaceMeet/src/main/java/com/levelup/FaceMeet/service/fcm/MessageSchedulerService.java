package com.levelup.FaceMeet.service.fcm;

import com.levelup.FaceMeet.domain.Setting;
import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.dto.FcmMessageDTO.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.fcm.ScheduledMessageRepository;
import com.levelup.FaceMeet.repository.admin.SettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSchedulerService {

    private final FcmMessageService fcmMessageService;
    private final ScheduledMessageRepository scheduledMessageRepository;
    private final TaskScheduler taskScheduler;
    private final SettingRepository settingRepository;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Value("${event.pre-message-minutes}")
    private int preMessageMinutes;

    @Value("${event.message-dispatch-minutes}")
    private int messageDispatchMinutes;

//    /**
//     * 이벤트 메시지 알림 스케줄링
//     */
//    @Transactional
//    public ScheduledTopicMessageResponse scheduleMessage(ScheduledTopicMessageRequest request) {
//
//        if(request.getScheduledTime() != null) {
//            if(request.getScheduledTime().isBefore(LocalDateTime.now())) {
//                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "예약 시간은 현재 시간 이전으로 설정할 수 없습니다.");
//            } else if(request.getScheduledTime().isBefore(LocalDateTime.now().plusMinutes(messageDispatchMinutes))) {
//                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
//                        String.format("예약 시간은 현재 시각으로부터 최소 %d분 이후여야 합니다.", messageDispatchMinutes));
//            }
//        }
//
//        Setting setting = settingRepository.findById(request.getSettingId())
//                .orElseThrow(() -> (new CustomException(ErrorCode.SETTING_NOT_FOUND)));
//
//        if(setting.getCurrentCnt() == 0) {
//            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 종료된 이벤트 입니다.");
//        }
//
//        if(setting.getEndTime() != null) {
//            if (setting.getEndTime().isBefore(LocalDateTime.now())) {
//                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 종료된 이벤트 입니다.");
//            } else if (setting.getEndTime().isBefore(request.getScheduledTime())) {
//                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "예약 시간은 이벤트 종료 전이어야 합니다.");
//            }
//        }
//
//        ScheduledMessage message = ScheduledMessage.builder()
//                    .settingId(request.getSettingId())
//                    .fcmTopicId(request.getFcmTopicId())
//                    .scheduledTime(request.getScheduledTime())
//                    .title(request.getTitle())
//                    .body(request.getBody())
//                    .messageData(request.getData())
//                .build();
//
//        ScheduledMessage scheduledMessage = scheduledMessageRepository.save(message);
//
//        scheduleMessageTask(scheduledMessage.getId());
//
//        return ScheduledTopicMessageResponse.builder()
//                .topicId(request.getFcmTopicId())
//                .build();
//    }
//
//    /**
//     * 사전 알림 스케줄링
//     */
//    public void scheduleMessageTask(Long settingId) {
//        Setting setting = settingRepository.findById(settingId)
//                .orElseThrow(() -> new CustomException(ErrorCode.SETTING_NOT_FOUND));
//
//        LocalDateTime preMessageTime = setting.getStartTime().minusMinutes(preMessageMinutes);
//
//        Runnable task = () -> {
//            try {
//                fcmMessageService.sendPreMessage(settingId);
//                log.info("사전 알림 메시지 스케줄링 완료: settingId={}, scheduledTime={}", settingId, preMessageTime);
//            } catch (Exception e) {
//                log.error("사전 알림 전송 실패: {}", e.getMessage(), e);
//            }
//        };
//
//        if (preMessageTime.isAfter(LocalDateTime.now())) {
//            taskScheduler.schedule(task, Date.from(preMessageTime.atZone(ZoneId.systemDefault()).toInstant()));
//        } else {
//            task.run();
//        }
//    }


    /**
     * 세팅 생성 시 예약 메시지 관련 작업 일괄 처리
     */
    public void scheduleAllTasks(Long settingId, ScheduledMessage savedMessage) {
        LocalDateTime startTime = savedMessage.getScheduledTime();

        // 스케줄 작업 키 생성
        String preMessageKey = "pre_" + settingId;
        String scheduledKey = "scheduled_" + savedMessage.getId();
        String sentKey = "sent_" + savedMessage.getId();

        // 사전 알림 스케줄링
        schedulePreMessage(settingId, startTime.minusMinutes(preMessageMinutes), preMessageKey);

        // 상태 변경 스케줄링
        // 이벤트 시작 시간 messageDispatchMinutes분 전에 SCHEDULED
        scheduleStatusUpdate(savedMessage.getId(), startTime.minusMinutes(messageDispatchMinutes), ScheduledMessage.MessageStatus.SCHEDULED, scheduledKey);
        // 이벤트 시작 시간에 SENT
        scheduleStatusUpdate(savedMessage.getId(), startTime, ScheduledMessage.MessageStatus.SENT, sentKey);
    }

    /**
     * 사전 알림 스케줄링
     */
    private void schedulePreMessage(Long settingId, LocalDateTime preMessageTime, String taskKey) {

        cancelScheduledTask(taskKey);

        if(preMessageTime.isAfter(LocalDateTime.now())) {
            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> {
                        fcmMessageService.sendPreMessage(settingId);
                        scheduledTasks.remove(taskKey);     // 실행 후 제거
                    },
                    Date.from(preMessageTime.atZone(ZoneId.systemDefault()).toInstant())
            );
            scheduledTasks.put(taskKey, future);
        } else {
            fcmMessageService.sendPreMessage(settingId);
        }
    }

    /**
     * 메세지 상태 변경 스케줄링
     */
    private void scheduleStatusUpdate(Long messageId, LocalDateTime targetTime, ScheduledMessage.MessageStatus status, String taskKey) {

        cancelScheduledTask(taskKey);

        if (targetTime.isAfter(LocalDateTime.now())) {
            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> {
                        updateMessageStatus(messageId, status);
                        scheduledTasks.remove(taskKey);
                    },
                    Date.from(targetTime.atZone(ZoneId.systemDefault()).toInstant())
            );
            scheduledTasks.put(taskKey, future);
        } else {
            updateMessageStatus(messageId, status); // 즉시 실행
        }
    }

    /**
     * 상태 업데이트
     */
    public void updateMessageStatus(Long messageId, ScheduledMessage.MessageStatus status) {
        scheduledMessageRepository.findById(messageId)
                .ifPresent(message -> {
                    message.setStatus(status);
                    scheduledMessageRepository.save(message);
                    log.info("메시지 상태 변경: {} -> {}", messageId, status);
                });
    }

    /**
     * 기존 스케줄된 작업 취소
     */
    public void cancelScheduledTask(String taskKey) {

        ScheduledFuture<?> existingTask = scheduledTasks.get(taskKey);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
            scheduledTasks.remove(taskKey);
            log.info("기존 스케줄 취소: {}", taskKey);
        }
    }

    /**
     * 세팅 관련 모든 스케줄 취소
     */
    public void cancelAllTasksForSetting(Long settingId) {
        // 해당 세팅의 사전 알림 메시지 취소
        String preMessageKey = "pre_" + settingId;
        cancelScheduledTask(preMessageKey);

        // 해당 세팅의 모든 예약 메시지 취소
        List<ScheduledMessage> messages = scheduledMessageRepository.findBySettingId(settingId);
        for (ScheduledMessage message : messages) {
            cancelScheduledTask("scheduled_" + message.getId());
            cancelScheduledTask("sent_" + message.getId());
        }
    }
}
