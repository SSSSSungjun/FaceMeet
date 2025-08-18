package com.levelup.FaceMeet.dto;

import com.levelup.FaceMeet.domain.Setting;
import com.levelup.FaceMeet.domain.User;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.util.Map;

public class SettingDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ValidTimeRange
    public static class SettingCreateRequest {
        @NotNull(message = "매칭권 개수는 필수입니다.")
        private Integer couponCount;

        @NotBlank(message = "이벤트 제목은 필수입니다.")
        private String title;

        @NotNull(message = "이벤트 시작 시간 설정은 필수입니다.")
        private LocalDateTime startTime;

        private LocalDateTime endTime;

        @NotBlank(message = "알림 메시지 제목은 필수입니다.")
        private String messageTitle;

        @NotBlank(message = "알림 메시지 내용은 필수입니다.")
        private String messageBody;

        private Map<String, String> data;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingUpdateRequest {
        private Integer couponCount;
        private String title;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingResponse {
        private Long settingId;
        private Long adminId;
        private String title;
        private Integer couponCount;
        private Integer currentCnt;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Boolean isActive;

        public static SettingResponse from(Setting setting) {
            return SettingResponse.builder()
                    .settingId(setting.getId())
                    .adminId(setting.getAdmin().getId())
                    .title(setting.getTitle())
                    .couponCount(setting.getCouponCount())
                    .currentCnt(setting.getCurrentCnt())
                    .startTime(setting.getStartTime())
                    .endTime(setting.getEndTime())
                    .createdAt(setting.getCreatedAt())
                    .updatedAt(setting.getUpdatedAt())
                    .isActive(setting.getIsActive())
                    .build();
        }
    }

    // 커스텀 검증 어노테이션
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = TimeRangeValidator.class)
    public @interface ValidTimeRange {
        String message() default "종료 시간은 시작 시간보다 이후여야 합니다.";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    // 검증 로직 구현
    public static class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, SettingCreateRequest> {

        @Override
        public void initialize(ValidTimeRange constraintAnnotation) {
        }

        @Override
        public boolean isValid(SettingCreateRequest request, ConstraintValidatorContext context) {
            if (request.getStartTime() == null || request.getEndTime() == null) {
                return true;
            }

            boolean isValid = request.getStartTime().isBefore(request.getEndTime());

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("종료 시간은 시작 시간보다 이후여야 합니다.")
                        .addConstraintViolation();
            }

            return isValid;
        }
    }
}
