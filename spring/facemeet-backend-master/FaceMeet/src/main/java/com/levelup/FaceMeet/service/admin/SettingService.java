package com.levelup.FaceMeet.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelup.FaceMeet.domain.Setting;
import com.levelup.FaceMeet.domain.User;
import com.levelup.FaceMeet.domain.fcm.FcmTopic;
import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.dto.SettingDTO.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.fcm.FcmTopicRepository;
import com.levelup.FaceMeet.repository.fcm.ScheduledMessageRepository;
import com.levelup.FaceMeet.repository.admin.SettingRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import com.levelup.FaceMeet.service.fcm.MessageSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;
    private final FcmTopicRepository fcmTopicRepository;
    private final ScheduledMessageRepository scheduledMessageRepository;
    private final MessageSchedulerService messageSchedulerService;

    @Value("${event.message-dispatch-minutes}")
    private int messageDispatchMinutes;

    @Value("${event.default-topic-name}")
    private String defaultTopicName;

    /**
     * 셋팅 생성 & 자동 메시지 예약
     */
    @Transactional
    public SettingResponse createSettingWithScheduling(Long adminId, SettingCreateRequest request) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        FcmTopic eventTopic = fcmTopicRepository.findByName(defaultTopicName)
                .orElseThrow(() -> new CustomException(ErrorCode.FCM_TOPIC_NOT_FOUND));

        validateTime(request.getStartTime(), request.getEndTime());

        Setting setting = Setting.builder()
                .admin(admin)
                .title(request.getTitle())
                .couponCount(request.getCouponCount())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        Setting savedSetting = settingRepository.save(setting);

        Map<String, String> data = new HashMap<>();
        data.put("type", "SCHEDULED_EVENT");
        data.put("settingId", savedSetting.getId().toString());
        data.put("title", request.getMessageTitle());
        data.put("body", request.getMessageBody());
        data.put("triggerTime", savedSetting.getStartTime().toString());

        // request.getData() 안의 값들도 추가
        if (request.getData() != null) {
            data.putAll(request.getData());
        }

        ScheduledMessage scheduledMessage = ScheduledMessage.builder()
                .settingId(savedSetting.getId())
                .fcmTopicId(eventTopic.getFcmTopicId())
                .scheduledTime(savedSetting.getStartTime())
                .title(request.getMessageTitle())
                .body(request.getMessageBody())
                .messageData(data)
                .status(ScheduledMessage.MessageStatus.PENDING)
                .build();

        ScheduledMessage savedMessage = scheduledMessageRepository.save(scheduledMessage);

        messageSchedulerService.scheduleAllTasks(savedSetting.getId(), savedMessage);

        return SettingResponse.from(savedSetting);
    }

    /**
     * 셋팅 수정
     */
    @Transactional
    public SettingResponse updateSetting(Long settingId, SettingUpdateRequest request) {

        Setting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTING_NOT_FOUND));

        if(setting.getStartTime().isBefore(LocalDateTime.now().plusMinutes(messageDispatchMinutes + 3))) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "수정할 수 없습니다.");
        }

        if (scheduledMessageRepository.existsBySettingIdAndStatusIn(settingId,
                List.of(ScheduledMessage.MessageStatus.SCHEDULED, ScheduledMessage.MessageStatus.SENT))) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 해당 세팅으로 발송된 메시지가 존재합니다. 세팅을 수정할 수 없습니다.");
        }

        // 제목 검증
        if(request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            setting.setTitle(request.getTitle());
        }

        // 쿠폰 개수 검증
        if(request.getCouponCount() != null) {
            if(request.getCouponCount() < setting.getCurrentCnt()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "쿠폰 개수는 현재 사용된 개수보다 적을 수 없습니다.");
            }
            setting.setCouponCount(request.getCouponCount());
        }

        // 시간 변경 시 검증
        LocalDateTime originalStartTime = setting.getStartTime();

        LocalDateTime newStartTime = request.getStartTime() != null ? request.getStartTime() : setting.getStartTime();
        LocalDateTime newEndTime = request.getEndTime() != null ? request.getEndTime() : setting.getEndTime();
        validateTime(newStartTime, newEndTime);

        boolean startTimeChanged = false;

        if(request.getStartTime() != null) {
            if(!request.getStartTime().equals(originalStartTime)) {
                startTimeChanged = true;
            }
            setting.setStartTime(request.getStartTime());
        }
        if(request.getEndTime() != null) {
            setting.setEndTime(request.getEndTime());
        }

        Setting updatedSetting = settingRepository.save(setting);

        // 시간 변경의 경우 예약 메시지 업데이트
        if (startTimeChanged) {
            updateScheduledMessagesForSetting(settingId, updatedSetting.getStartTime());
        }

        return SettingResponse.from(updatedSetting);
    }

    /**
     * 셋팅의 시작 시간 변경 시 관련 예약 메시지들 업데이트
     */
    private void updateScheduledMessagesForSetting(Long settingId, LocalDateTime newStartTime) {
        // 해당 세팅 관련 메시지 작업 스케줄 모두 취소
        messageSchedulerService.cancelAllTasksForSetting(settingId);

        List<ScheduledMessage> scheduledMessages = scheduledMessageRepository
                .findBySettingIdAndStatusIn(settingId, List.of(ScheduledMessage.MessageStatus.PENDING));

        for(ScheduledMessage message : scheduledMessages) {
            try {
                // 기존 메시지 시간 변경
                message.setScheduledTime(newStartTime);
                scheduledMessageRepository.save(message);

                messageSchedulerService.scheduleAllTasks(message.getId(), message);
                log.info("Setting {} 예약 메시지 시간 변경 및 재스케줄링: messageId={}", settingId, message.getId());
            } catch (Exception e) {
                log.error("예약 메시지 업데이트 실패", e);
                throw new CustomException(ErrorCode.MESSAGE_SCHEDULING_FAILED);
            }
        }
    }

    /**
     * 셋팅 (관련 예약 메시지) 삭제
     */
    @Transactional
    public void deleteSetting(Long settingId) {
        Setting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTING_NOT_FOUND));

        if(setting.getStartTime().isBefore(LocalDateTime.now().plusMinutes(messageDispatchMinutes + 3))) {
            throw new CustomException(ErrorCode.SETTING_CONFLICT, "예약 메시지 발송 시간 이후이므로 세팅을 삭제할 수 없습니다.");
        }

        messageSchedulerService.cancelAllTasksForSetting(settingId);

        List<ScheduledMessage> scheduledMessages = scheduledMessageRepository.findBySettingId(settingId);
        for(ScheduledMessage message : scheduledMessages) {
            message.setStatus(ScheduledMessage.MessageStatus.CANCELLED);
            scheduledMessageRepository.save(message);
        }

        setting.setIsActive(false);
        settingRepository.save(setting);
    }

    /**
     * 특정 셋팅 조회
     */
    public SettingResponse getSetting(Long settingId) {
        Setting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTING_NOT_FOUND));

        return SettingResponse.from(setting);
    }

    /**
     * 셋팅 목록 조회
     */
    public List<SettingResponse> getSettings() {
        return settingRepository.findAllByIsActiveTrueOrderByStartTimeDesc()
                .stream()
                .map(SettingResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 셋팅 시간 검증
     */
    private void validateTime(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();

        if (startTime != null) {
            LocalDateTime minAllowedTime = now.plusMinutes(messageDispatchMinutes);

            if (startTime.isBefore(minAllowedTime)) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                        String.format("시작 시간은 현재 시각으로부터 최소 %d분 이후여야 합니다.", messageDispatchMinutes));
            }
        }

        if (endTime != null) {
            if (endTime.isBefore(now)) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "종료 시간은 현재 시간 이전으로 설정할 수 없습니다.");
            }

            if (startTime != null && endTime.isBefore(startTime)) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "종료 시간은 시작 시간보다 이후여야 합니다.");
            }
        }
    }
}