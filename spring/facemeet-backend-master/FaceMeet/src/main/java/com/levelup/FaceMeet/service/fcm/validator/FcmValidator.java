package com.levelup.FaceMeet.service.fcm.validator;

import com.levelup.FaceMeet.config.fcm.FcmProperties;
import com.levelup.FaceMeet.domain.Setting;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * FCM 관련 검증 유틸리티
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FcmValidator {

    private final FcmProperties fcmProperties;

    /**
     * 스케줄 시간 검증
     */
    public void validateScheduleTime(LocalDateTime scheduledTime) {
        if (scheduledTime == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "예약 시간은 필수입니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (scheduledTime.isBefore(now)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "예약 시간은 현재 시간 이후여야 합니다.");
        }

        LocalDateTime minScheduleTime = now.plusMinutes(
                fcmProperties.getMessageDispatchMinutes()
        );

        if (scheduledTime.isBefore(minScheduleTime)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("예약 시간은 현재 시각으로부터 최소 %d분 이후여야 합니다.", fcmProperties.getMessageDispatchMinutes()));
        }
    }

    /**
     * 세팅 유효성 검증
     */
    public void validateSetting(Setting setting, LocalDateTime scheduledTime) {
        if (setting == null) {
            throw new CustomException(ErrorCode.SETTING_NOT_FOUND);
        }

        if (setting.getCurrentCnt() == 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 종료된 이벤트입니다.");
        }

        if (setting.getEndTime() != null) {
            if (setting.getEndTime().isBefore(LocalDateTime.now())) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 종료된 이벤트입니다.");
            }

            if (scheduledTime != null && setting.getEndTime().isBefore(scheduledTime)) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "예약 시간은 이벤트 종료 전이어야 합니다.");
            }
        }
    }

    /**
     * 토큰 유효성 검증
     */
    public void validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "FCM 토큰이 유효하지 않습니다.");
        }

        if (token.length() < 100) {  // FCM 토큰은 일반적으로 150자 이상
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "FCM 토큰 형식이 올바르지 않습니다.");
        }
    }

    /**
     * 메시지 내용 검증
     */
    public void validateMessageContent(String title, String body) {
        if (title == null || title.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "메시지 제목은 필수입니다.");
        }

        if (body == null || body.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "메시지 내용은 필수입니다.");
        }

        // FCM 제한사항
        if (title.length() > 200) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "메시지 제목은 200자를 초과할 수 없습니다.");
        }

        if (body.length() > 4000) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "메시지 내용은 4000자를 초과할 수 없습니다.");
        }
    }

    /**
     * 배치 크기 검증
     */
    public void validateBatchSize(int size) {
        if (size > 500) {  // FCM 멀티캐스트 제한
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "한 번에 전송 가능한 최대 토큰 수는 500개입니다.");
        }
    }
}