package com.levelup.FaceMeet.service.setting.helper.validator;

import com.levelup.FaceMeet.config.fcm.FcmProperties;
import com.levelup.FaceMeet.domain.Setting;
import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.dto.SettingDTO.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.fcm.ScheduledMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SettingValidator {

    private final FcmProperties fcmProperties;
    private final ScheduledMessageRepository messageRepository;

    /**
     * 생성 시 검증
     */
    public void validateCreation(SettingCreateRequest request) {
        validateStartTime(request.getStartTime());
        validateEndTime(request.getStartTime(), request.getEndTime());
        validateCouponCount(request.getCouponCount());
    }

    /**
     * 수정 시 검증
     */
    public void validateUpdate(Setting setting, SettingUpdateRequest request) {
        if (!isModifiable(setting)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이벤트 시작이 임박하여 수정할 수 없습니다.");
        }

        if (hasScheduledMessages(setting.getId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 해당 세팅으로 발송된 메시지가 있어 수정할 수 없습니다.");
        }

        if (request != null && request.getCouponCount() != null) {
            validateCouponCountUpdate(setting, request.getCouponCount());
        }
    }

    /**
     * 삭제 시 검증
     */
    public void validateDeletion(Setting setting) {
        if (!isModifiable(setting)) {
            throw new CustomException(ErrorCode.SETTING_CONFLICT, "이벤트 시작이 임박하여 삭제할 수 없습니다.");
        }
    }



    // =============== 헬퍼 메소드 ================
    /**
     * 발송 시간 임박 여부 확인
     */
    private boolean isModifiable(Setting setting) {
        LocalDateTime minTime = LocalDateTime.now()
                .plusMinutes(fcmProperties.getMessageDispatchMinutes() + 3);
        return setting.getStartTime().isAfter(minTime);
    }

    /**
     * 발송 상태 예약 메시지 조회
     */
    private boolean hasScheduledMessages(Long settingId) {
        return messageRepository.existsBySettingIdAndStatusIn(
                settingId,
                List.of(ScheduledMessage.MessageStatus.SCHEDULED, ScheduledMessage.MessageStatus.SENT)
        );
    }

    /**
     * 유효 시작 시간 검증
     */
    private void validateStartTime(LocalDateTime start) {
        if (start == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "시작 시간은 필수입니다.");
        }

        LocalDateTime minTime = LocalDateTime.now()
                .plusMinutes(fcmProperties.getMessageDispatchMinutes());

        if (start.isBefore(minTime)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("시작 시간은 현재로부터 최소 %d분 이후여야 합니다.",
                            fcmProperties.getMessageDispatchMinutes()));
        }
    }

    /**
     * 유효 종료 시간 검증
     */
    private void validateEndTime(LocalDateTime start, LocalDateTime end) {
        if (end != null && end.isBefore(start)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "종료 시간은 시작 시간보다 이후여야 합니다.");
        }
    }

    /**
     * 쿠폰 개수 검증
     */
    private void validateCouponCount(Integer count) {
        if (count == null || count < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "쿠폰은 0개 이상이어야 합니다.");
        }
    }

    private void validateCouponCountUpdate(Setting setting, Integer newCount) {
        if (newCount < setting.getCurrentCnt()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "쿠폰 개수는 현재 사용된 개수보다 적을 수 없습니다.");
        }
    }
}