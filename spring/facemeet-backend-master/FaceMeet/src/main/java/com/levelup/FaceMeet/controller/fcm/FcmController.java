package com.levelup.FaceMeet.controller.fcm;

import com.levelup.FaceMeet.dto.FcmMessageDTO.*;
import com.levelup.FaceMeet.security.dto.CustomUserDetails;
import com.levelup.FaceMeet.service.fcm.FcmMessageService;
import com.levelup.FaceMeet.service.fcm.FcmTokenService;
import com.levelup.FaceMeet.service.fcm.FcmTopicService;
import com.levelup.FaceMeet.service.fcm.MessageSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
@Tag(name = "FcmController", description = "유저 FCM 사용 기능 제공")
public class FcmController {

    private final FcmMessageService fcmMessageService;
    private final FcmTokenService fcmTokenService;
    private final FcmTopicService fcmTopicService;
    private final MessageSchedulerService messageSchedulerService;

    // === 토큰 관리 ===
    @PutMapping("/tokens")
    @Operation(summary = "FCM 토큰 등록/갱신", description = "사용자 기기별 토큰을 등록하거나 갱신합니다.")
    public ResponseEntity<FcmTokenResponse> upsertToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid FcmTokenUpsertRequest request) {

        log.debug("토큰 등록/갱신: 사용자 {}", userDetails.getUserId());

        FcmTokenResponse response = fcmTokenService.upsertToken(userDetails.getUserId(), request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tokens")
    @Operation(summary = "FCM 토큰 삭제", description = "현재 사용자의 특정 토큰을 삭제합니다 (로그아웃시 사용).")
    public ResponseEntity<?> deleteToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid FcmTokenDeleteRequest request) {

        log.debug("토큰 삭제: 사용자 {}", userDetails.getUserId());

        fcmTokenService.deleteToken(userDetails.getUserId(), request.getDeviceToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tokens")
    @Operation(summary = "내 FCM 토큰 조회", description = "현재 사용자의 등록된 모든 토큰을 조회합니다.")
    public ResponseEntity<List<FcmTokenResponse>> getMyTokens(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<FcmTokenResponse> tokens = fcmTokenService.getUserTokens(userDetails.getUserId());
        return ResponseEntity.ok(tokens);
    }

    // === 토픽 구독 관리 ===
    @PostMapping("/topics/{topicId}/subscriptions")
    @Operation(summary = "FCM 토픽 구독", description = "현재 사용자의 모든 활성 기기를 지정한 토픽에 구독시킵니다.")
    public ResponseEntity<FcmTopicSubscriptionResponse> subscribeToTopic(
            @PathVariable Long topicId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.debug("토픽 구독: 사용자 {}, 토픽 {}", userDetails.getUserId(), topicId);

        FcmTopicSubscriptionResponse response = fcmTopicService.subscribeTopic(userDetails.getUserId(), topicId);
        if(topicId == 1L) {
            fcmMessageService.sendScheduledMessages(userDetails.getUserId());
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/topics/{topicId}/subscriptions")
    @Operation(summary = "FCM 토픽 구독 취소", description = "현재 사용자의 모든 기기를 지정한 토픽에서 구독 해제합니다.")
    public ResponseEntity<FcmTopicSubscriptionResponse> unsubscribeFromTopic(
            @PathVariable Long topicId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.debug("토픽 구독 취소: 사용자 {}, 토픽 {}", userDetails.getUserId(), topicId);

        FcmTopicSubscriptionResponse response = fcmTopicService.unsubscribeTopic(userDetails.getUserId(), topicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/topics/subscriptions")
    @Operation(summary = "내 구독 토픽 조회", description = "현재 사용자가 구독 중인 모든 토픽을 조회합니다.")
    public ResponseEntity<UserSubscriptionResponse> getMySubscriptions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserSubscriptionResponse response = fcmTopicService.getUserSubscriptions(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }
}