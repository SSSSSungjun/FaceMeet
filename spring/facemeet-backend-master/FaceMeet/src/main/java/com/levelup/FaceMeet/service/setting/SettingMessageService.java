package com.levelup.FaceMeet.service.setting;

import com.levelup.FaceMeet.config.fcm.FcmConstants;
import com.levelup.FaceMeet.config.fcm.FcmProperties;
import com.levelup.FaceMeet.domain.Setting;
import com.levelup.FaceMeet.domain.fcm.FcmTopic;
import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.dto.FcmMessageDTO;
import com.levelup.FaceMeet.dto.SettingDTO;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.admin.SettingRepository;
import com.levelup.FaceMeet.repository.fcm.FcmTopicRepository;
import com.levelup.FaceMeet.repository.fcm.ScheduledMessageRepository;
import com.levelup.FaceMeet.service.fcm.builder.FcmMessageBuilder;
import com.levelup.FaceMeet.service.fcm.scheduler.FcmMessageSchedulingService;
import com.levelup.FaceMeet.service.setting.helper.validator.SettingValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingMessageService {

    private final FcmProperties fcmProperties;

    private final FcmTopicRepository topicRepository;
    private final SettingRepository settingRepository;
    private final ScheduledMessageRepository scheduledMessageRepository;

    private final FcmMessageSchedulingService schedulingService;
    private final SettingValidator settingValidator;
    private final FcmMessageBuilder fcmMessageBuilder;

    /**
     * 세팅 생성 시 메시지 예약
     */
    @Transactional
    public void handleSettingCreated(Long settingId, SettingDTO.SettingCreateRequest request) {
        try {
            FcmTopic eventTopic = topicRepository.findByName(fcmProperties.getDefaultTopicName())
                    .orElseThrow(() -> new CustomException(ErrorCode.FCM_TOPIC_NOT_FOUND));

            Map<String, String> data = fcmMessageBuilder.buildScheduledData(
                    request.getMessageTitle(),
                    request.getMessageBody(),
                    request.getMessageData(),
                    settingId,
                    request.getStartTime());

            // 메시지 생성
            ScheduledMessage message = ScheduledMessage.builder()
                    .settingId(settingId)
                    .fcmTopicId(eventTopic.getFcmTopicId())
                    .scheduledTime(request.getStartTime())
                    .title(request.getMessageTitle())
                    .body(request.getMessageBody())
                    .messageData(data)
                    .status(ScheduledMessage.MessageStatus.PENDING)
                    .build();

            ScheduledMessage savedMessage = scheduledMessageRepository.save(message);

            // 스케줄링
            schedulingService.scheduleAllTasks(settingId, savedMessage);

            log.info("메시지 예약 완료: settingId={}, messageId={}", settingId, savedMessage.getId());

        } catch (Exception e) {
            log.error("메시지 예약 실패: settingId={}", settingId, e);
        }
    }

    /**
     * 세팅 시작 시간 변경 시 재스케줄링
     */
    @Transactional
    public void handleSettingTimeChanged(Long settingId, LocalDateTime newStartTime) {
        // 기존 스케줄 취소
        schedulingService.cancelAllTasksForSetting(settingId);

        // PENDING 메시지들 재스케줄링
        List<ScheduledMessage> messages = scheduledMessageRepository.findBySettingIdAndStatus(settingId, ScheduledMessage.MessageStatus.PENDING);

        for (ScheduledMessage message : messages) {
            message.setScheduledTime(newStartTime);

            Map<String, String> updatedData = message.getMessageData();
            updatedData.put(FcmConstants.DATA_TRIGGER_TIME, newStartTime.toString());
            message.setMessageData(updatedData);

            scheduledMessageRepository.save(message);
            schedulingService.scheduleAllTasks(settingId, message);
        }

        log.info("재스케줄링 완료: settingId={}, 메시지 수={}", settingId, messages.size());
    }

    /**
     * 세팅 삭제 시 메시지 취소
     */
    @Transactional
    public void handleSettingDeleted(Long settingId) {
        // 스케줄 취소
        schedulingService.cancelAllTasksForSetting(settingId);

        // 메시지 상태 변경
        scheduledMessageRepository.updateStatusBySettingId(settingId, ScheduledMessage.MessageStatus.CANCELLED);

        log.info("메시지 취소 완료: settingId={}", settingId);
    }

    @Transactional(readOnly = true)
    public FcmMessageDTO.ScheduledMessageResponse getMessageBySettingId(Long settingId) {
        ScheduledMessage message = scheduledMessageRepository.findBySettingId(settingId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULED_MESSAGE_NOT_FOUND));

        return FcmMessageDTO.ScheduledMessageResponse.from(message);
    }

    @Transactional
    public List<FcmMessageDTO.ScheduledMessageResponse> updateMessagesBySettingId(Long settingId, FcmMessageDTO.ScheduledMessageUpdateRequest request) {
        Setting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTING_NOT_FOUND));

        settingValidator.validateUpdate(setting, null);

        List<ScheduledMessage> messages = scheduledMessageRepository.findBySettingId(settingId);

        if (messages.isEmpty()) {
            throw new CustomException(ErrorCode.SCHEDULED_MESSAGE_NOT_FOUND);
        }

        List<FcmMessageDTO.ScheduledMessageResponse> response = new ArrayList<>();

        for(ScheduledMessage message : messages) {

            if(request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
                message.setTitle(request.getTitle());
            }
            if(request.getBody() != null && !request.getBody().trim().isEmpty()) {
                message.setBody(request.getBody());
            }
            if(request.getMessageData() != null && !request.getMessageData().isEmpty()) {
                message.setMessageData(request.getMessageData());
            }

            ScheduledMessage savedMessage = scheduledMessageRepository.save(message);
            response.add(FcmMessageDTO.ScheduledMessageResponse.from(savedMessage));
        }

        return response;
    }

}