package com.levelup.FaceMeet.controller.admin;

import com.levelup.FaceMeet.dto.FcmMessageDTO;
import com.levelup.FaceMeet.dto.FcmMessageDTO.*;
import com.levelup.FaceMeet.security.dto.CustomUserDetails;
import com.levelup.FaceMeet.service.fcm.messaging.ImmediateMessagingService;
import com.levelup.FaceMeet.service.fcm.FcmTopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/fcm")
@RequiredArgsConstructor
@Tag(name = "FcmAdminController", description = "어드민 FCM 관리 기능 제공")
public class FcmAdminController {

    private final ImmediateMessagingService immediateMessagingService;
    private final FcmTopicService fcmTopicService;

    // === 메시지 전송 ===
    @PostMapping("/messages/token")
    @Operation(summary = "FCM 토큰 기반 메시지 전송", description = "지정한 FCM 디바이스 토큰을 가진 단일 사용자에게 알림 메시지를 전송합니다.")
    public ResponseEntity<?> sendTokenMessage(@RequestBody FcmTokenMessageRequest request) {
        log.debug("메시지를 전송합니다.");

        immediateMessagingService.sendMessageByToken(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/messages/topic")
    @Operation(summary = "FCM 토픽 기반 메시지 전송", description = "지정한 FCM 토픽을 구독한 모든 사용자에게 알림 메시지를 브로드캐스트 방식으로 전송합니다.")
    public ResponseEntity<?> sendTopicMessage(@RequestBody @Valid FcmTopicMessageRequest request) {
        log.debug("토픽 기반 메시지 전송: {}", request.getTopicId());

        immediateMessagingService.sendMessageByTopic(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/messages/user")
    @Operation(summary = "사용자별 메시지 전송", description = "특정 사용자의 모든 활성 기기로 메시지를 전송합니다.")
    public ResponseEntity<?> sendMessageToUser(@RequestBody @Valid FcmUserMessageRequest request) {
        log.debug("사용자 기반 메시지 전송: {}", request.getUserId());

        immediateMessagingService.sendMessageByUserId(request);
        return ResponseEntity.ok().build();
    }

    // === 토픽 관리 ===
    @PostMapping("/topics")
    @Operation(summary = "FCM 토픽 등록", description = "새로운 알림 토픽을 등록합니다.")
    public ResponseEntity<?> createTopic(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid FcmMessageDTO.FcmTopicUpsertRequest request) {

        log.debug("토픽 생성: 토픽 {}", request.getName());

        fcmTopicService.createTopic(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/topics/{topicId}")
    @Operation(summary = "FCM 토픽 삭제", description = "알림 토픽을 제거합니다.")
    public ResponseEntity<?> deleteTopic(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long topicId) {

        log.debug("토픽 제거: topicId: {}", topicId);

        fcmTopicService.deleteTopic(topicId);
        return ResponseEntity.ok().build();
    }
}
