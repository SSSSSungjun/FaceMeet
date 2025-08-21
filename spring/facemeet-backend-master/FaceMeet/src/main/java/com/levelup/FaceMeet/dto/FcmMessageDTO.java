package com.levelup.FaceMeet.dto;

import com.levelup.FaceMeet.domain.Setting;
import com.levelup.FaceMeet.domain.fcm.FcmToken;
import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class FcmMessageDTO {

    /**
     * 유저 ID 기반 메시지 요청
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class FcmUserMessageRequest {
        @NotNull(message = "유저 ID는 필수입니다.")
        private Long userId;

        @NotBlank(message = "제목은 필수입니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String body;

        private Map<String, String> data;
    }

    /**
     * 토큰 기반 메시지 요청
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class FcmTokenMessageRequest {
        @NotBlank(message = "제목은 필수입니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String body;

        @NotBlank(message = "토큰은 필수입니다.")
        @Size(max = 500, message = "토큰 길이가 너무 깁니다.")
        private String token;

        private Map<String, String> data;
    }

    /**
     * 토큰 응답
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class FcmTokenResponse {
        private Long tokenId;
        private String deviceToken;
        private FcmToken.DeviceType deviceType;
        private Boolean isNew;
        private LocalDateTime lastUsedAt;
    }

    /**
     * 토큰 등록/갱신 요청
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class FcmTokenUpsertRequest {
        @NotBlank(message = "디바이스 토큰은 필수입니다.")
        private String deviceToken;

        @NotNull(message = "디바이스 타입은 필수입니다.")
        private FcmToken.DeviceType deviceType;

        @Size(max = 100, message = "디바이스 ID 길이가 너무 깁니다.")
        private String deviceId;

        // @Size(max = 50, message = "디바이스 모델명이 너무 깁니다.")
        // private String deviceModel;

        // @Size(max = 20, message = "앱 버전이 너무 깁니다.")
        // private String appVersion;

//        private String previousToken;
    }

    /**
     * 토큰 삭제 요청
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class FcmTokenDeleteRequest {
        @NotBlank(message = "디바이스 토큰은 필수입니다.")
        private String deviceToken;
    }

    /**
     * 토픽 생성 요청
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class FcmTopicUpsertRequest {
        @NotBlank(message = "토픽 이름은 필수입니다.")
        private String name;

        @Size(max = 255, message = "설명 길이가 너무 깁니다.")
        private String description;
    }

    /**
     * 토픽 기반 메시지 요청
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class FcmTopicMessageRequest {
        @NotBlank(message = "제목은 필수입니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String body;

        @NotNull(message = "토픽은 필수입니다.")
        private Long topicId;

        private Map<String, String> data;
    }

    /**
     * 토픽 구독 응답
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class FcmTopicSubscriptionResponse {
        private Long topicId;
        private Long userId;
        private Integer subscribedDeviceCount;
        private List<String> subscribedTokens;
        private List<String> failedTokens;
    }

    /**
     * 유저 구독 목록 조회 응답
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class UserSubscriptionResponse {
        private Long userId;
        private List<String> topicName;
    }

    /**
     * 예약 토픽 기반 메시지 요청
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class ScheduledTopicMessageRequest {
        @NotNull(message = "이벤트 id는 필수입니다.")
        private Long settingId;

        @NotNull(message = "토픽은 필수입니다.")
        private Long fcmTopicId;

        @NotNull
        @Future(message = "목표 시간은 미래여야 합니다")
        private LocalDateTime scheduledTime; // 사용자 도달 목표 시간

        @NotBlank(message = "제목은 필수입니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String body;

        private Map<String, String> data;

//        private Boolean highPriority = false;   // 높은 우선순위 전송
//        private Integer maxBufferMinutes = 30;  // 최대 버퍼 시간 제한
    }

    /**
     * 예약 토픽 기반 메시지 응답
     */
    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class ScheduledTopicMessageResponse {
        private Long messageId;
        private Long topicId;
        private LocalDateTime scheduledSendTime;    // FCM 전송 시작 시간
        private LocalDateTime targetDeliveryTime;   // 사용자 도달 목표 시간
        private Long estimatedUserCount;
        private Integer bufferMinutes;
        private String status; // SCHEDULED, SENDING, COMPLETED, FAILED
        private String message;
    }

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class ScheduledMessageUpdateRequest {
        private String title;
        private String body;
        private Map<String, String> messageData;
    }

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class ScheduledMessageResponse {
        private Long scheduledMessageId;
        private Long settingId;
        private String title;
        private String body;
        private Map<String, String> messageData;
        private String status; // SCHEDULED, SENDING, COMPLETED, FAILED

        public static ScheduledMessageResponse from(ScheduledMessage message) {
            return ScheduledMessageResponse.builder()
                    .scheduledMessageId(message.getId())
                    .settingId(message.getSettingId())
                    .title(message.getTitle())
                    .body(message.getBody())
                    .messageData(message.getMessageData())
                    .status(message.getStatus().toString())
                    .build();
        }
    }

    @Builder @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class NotificationResponse {
        private Long notificationId;
        private Long settingId;
        private String title;
        private String body;
        private Map<String, String> messageData;
        private LocalDateTime scheduledTime;
        private Boolean isRead;

        public static NotificationResponse from(ScheduledMessage message, Boolean isRead) {
            return NotificationResponse.builder()
                    .notificationId(message.getId())
                    .settingId(message.getSettingId())
                    .title(message.getTitle())
                    .body(message.getBody())
                    .messageData(message.getMessageData())
                    .scheduledTime(message.getScheduledTime())
                    .isRead(isRead)
                    .build();
        }
    }
}
