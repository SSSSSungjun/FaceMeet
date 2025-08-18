package com.levelup.FaceMeet.service.fcm;

import com.levelup.FaceMeet.domain.Setting;
import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.dto.FcmMessageDTO.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.dto.FcmMessageDTO.ScheduledMessageUpdateRequest;
import com.levelup.FaceMeet.repository.admin.SettingRepository;
import com.levelup.FaceMeet.repository.fcm.ScheduledMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledMessageService {

    private final ScheduledMessageRepository scheduledMessageRepository;
    private final MessageSchedulerService messageSchedulerService;
    private final SettingRepository settingRepository;

    @Value("${event.message-dispatch-minutes}")
    private int messageDispatchMinutes;

    @Transactional(readOnly = true)
    public ScheduledMessageResponse getScheduledMessageById(Long scheduledMessageId) {
        ScheduledMessage message = scheduledMessageRepository.findById(scheduledMessageId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULED_MESSAGE_NOT_FOUND));

        return ScheduledMessageResponse.from(message);
    }

    @Transactional
    public ScheduledMessageResponse updateScheduledMessage(Long messageId, ScheduledMessageUpdateRequest request) {
        ScheduledMessage message = scheduledMessageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULED_MESSAGE_NOT_FOUND));

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

        return ScheduledMessageResponse.from(savedMessage);
    }

    @Transactional
    public void deleteScheduledMessage(Long scheduledMessageId) {
        ScheduledMessage message = scheduledMessageRepository.findById(scheduledMessageId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULED_MESSAGE_NOT_FOUND));

        messageSchedulerService.cancelScheduledTask("scheduled_" + scheduledMessageId);
        messageSchedulerService.cancelScheduledTask("sent_" + scheduledMessageId);
        message.setStatus(ScheduledMessage.MessageStatus.CANCELLED);

        scheduledMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public ScheduledMessageResponse getScheduledMessageBySettingId(Long settingId) {
        ScheduledMessage message = scheduledMessageRepository.findBySettingId(settingId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULED_MESSAGE_NOT_FOUND));

        return ScheduledMessageResponse.from(message);
    }

    @Transactional
    public List<ScheduledMessageResponse> updateScheduledMessagesBySettingId(Long settingId, ScheduledMessageUpdateRequest request) {
        Setting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTING_NOT_FOUND));

        if(setting.getStartTime().isBefore(LocalDateTime.now().plusMinutes(messageDispatchMinutes + 3))) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "수정할 수 없습니다.");
        }

        if (scheduledMessageRepository.existsBySettingIdAndStatusIn(settingId,
                List.of(ScheduledMessage.MessageStatus.SCHEDULED, ScheduledMessage.MessageStatus.SENT))) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 해당 세팅으로 발송된 메시지가 존재합니다. 세팅을 수정할 수 없습니다.");
        }

        List<ScheduledMessage> messages = scheduledMessageRepository.findBySettingId(settingId);

        if (messages.isEmpty()) {
            throw new CustomException(ErrorCode.SCHEDULED_MESSAGE_NOT_FOUND);
        }

        List<ScheduledMessageResponse> response = new ArrayList<>();

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
            response.add(ScheduledMessageResponse.from(savedMessage));
        }

        return response;
    }
}
