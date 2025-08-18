package com.levelup.FaceMeet.controller.admin;

import com.levelup.FaceMeet.dto.FcmMessageDTO;
import com.levelup.FaceMeet.dto.FcmMessageDTO.*;
import com.levelup.FaceMeet.dto.SettingDTO.*;
import com.levelup.FaceMeet.security.dto.CustomUserDetails;
import com.levelup.FaceMeet.service.admin.SettingService;
import com.levelup.FaceMeet.service.fcm.ScheduledMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/settings")
@Tag(name = "SettingAdminController", description = "어드민이 이벤트 세팅 생성/수정/조회 기능 제공")
public class SettingAdminController {

    private final SettingService settingService;
    private final ScheduledMessageService scheduledMessageService;

    @PostMapping
    @Operation(summary = "이벤트 세팅 생성", description = "이벤트 세팅을 생성하고 자동으로 알림 메시지를 예약합니다.")
    public ResponseEntity<SettingResponse> createSetting(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid SettingCreateRequest request) {

        SettingResponse response = settingService.createSettingWithScheduling(userDetails.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{settingId}")
    @Operation(summary = "이벤트 세팅 수정", description = "이벤트 세팅을 수정하는 기능입니다.")
    public ResponseEntity<SettingResponse> updateSetting(@PathVariable Long settingId, @RequestBody @Valid SettingUpdateRequest request) {

        SettingResponse response = settingService.updateSetting(settingId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{settingId}")
    @Operation(summary = "이벤트 세팅 삭제", description = "이벤트 세팅을 비활성화 하는 기능입니다.")
    public ResponseEntity<Void> deleteSetting(@PathVariable Long settingId) {

        settingService.deleteSetting(settingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{settingId}")
    @Operation(summary = "이벤트 세팅 조회", description = "이벤트 세팅 정보를 조회하는 기능입니다.")
    public ResponseEntity<SettingResponse> getSetting(@PathVariable Long settingId) {

        SettingResponse response = settingService.getSetting(settingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "전체 이벤트 세팅 조회", description = "전체 이벤트 세팅 목록을 조회하는 기능입니다.")
    public ResponseEntity<List<SettingResponse>> getSettings() {

        List<SettingResponse> responses = settingService.getSettings();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{settingId}/messages")
    @Operation(summary = "특정 이벤트 예약 메시지 조회", description = "특정 이벤트 세팅의 예약 메시지를 조회합니다.")
    public ResponseEntity<ScheduledMessageResponse> getSettingMessage(@PathVariable Long settingId) {

        ScheduledMessageResponse response = scheduledMessageService.getScheduledMessageBySettingId(settingId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{settingId}/messages")
    @Operation(summary = "특정 이벤트 예약 메시지 일괄 수정", description = "특정 이벤트 세팅의 예약 메시지를 일괄 수정합니다.")
    public ResponseEntity<List<ScheduledMessageResponse>> updateSettingMessages(@PathVariable Long settingId, @RequestBody @Valid ScheduledMessageUpdateRequest request) {

        List<ScheduledMessageResponse> response = scheduledMessageService.updateScheduledMessagesBySettingId(settingId, request);
        return ResponseEntity.ok(response);
    }
}
