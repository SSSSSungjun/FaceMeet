package com.levelup.FaceMeet.controller.user;

import com.levelup.FaceMeet.dto.FaceDTO.*;
import com.levelup.FaceMeet.dto.FcmMessageDTO.NotificationResponse;
import com.levelup.FaceMeet.dto.UserInfoDTO.*;
import com.levelup.FaceMeet.security.dto.CustomUserDetails;
import com.levelup.FaceMeet.service.fcm.UserNotificationHistoryService;
import com.levelup.FaceMeet.service.user.UserProfileService;
import com.levelup.FaceMeet.service.fcm.FcmMessageService;
import com.levelup.FaceMeet.service.user.FaceService;
import com.levelup.FaceMeet.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "회원 정보 조회/수정 기능 제공")
public class UserController {

    private final UserService userService;
    private final FcmMessageService fcmMessageService;
    private final UserProfileService userProfileService;
    private final FaceService faceService;
    private final UserNotificationHistoryService userNotificationHistoryService;

    @GetMapping("/home")
    @Operation(summary = "메인 화면 정보 조회", description = "회원별 홈화면 필요 정보 조회 기능입니다.")
    public ResponseEntity<UserHomeInfoResponse> getUserHomeInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {

        UserHomeInfoResponse userHomeInfo  = userProfileService.getUserHomeInfo(userDetails.getUserId());

        return ResponseEntity.ok(userHomeInfo);
    }

    @GetMapping("/me")
    @Operation(summary = "회원 정보 조회", description = "회원 정보를 조회할 수 있는 기능입니다.")
    public ResponseEntity<UserInfoResponse> getUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {

        UserInfoResponse userInfo  = userService.getUserInfo(userDetails.getUserId());

        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/me/status")
    @Operation(summary = "회원 정보 상태 조회", description = "회원 정보 상태(hasInfo, hasFace) 를 조회할 수 있는 기능입니다.")
    public ResponseEntity<UserInfoStatusResponse> getUserInfoStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {

        UserInfoStatusResponse userInfoStatus  = userService.getUserInfoStatus(userDetails.getUserId());

        return ResponseEntity.ok(userInfoStatus);
    }

    @GetMapping("/me/face")
    @Operation(summary = "회원 관상 조회", description = "회원 관상 정보를 조회할 수 있는 기능입니다.")
    public ResponseEntity<UserFaceInfoResponse> getUserFaceInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {

        UserFaceInfoResponse response  = userProfileService.getUserFaceInfo(userDetails.getUserId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/face")
    @Operation(summary = "회원 관상 변경 알림", description = "회원 관상 정보 변경을 스프링 서버에 알리기 위한 기능입니다.")
    public ResponseEntity<Void> updateUserFace(@AuthenticationPrincipal CustomUserDetails userDetails) {

        faceService.evictUserFaceCache(userDetails.getUserId());

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me")
    @Operation(summary = "회원 정보 수정", description = "회원 정보를 수정할 수 있는 기능입니다.")
    public ResponseEntity<UserInfoResponse> updateUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UserInfoUpdateRequest userInfoUpdateRequest) {

        UserInfoResponse response = userService.updateUser(userDetails.getUserId(), userInfoUpdateRequest);

        return ResponseEntity.ok(response);
    }

    //캐싱해 둔 회원 정보에서 해당 회원 삭제
    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "소셜 로그인 연결 해제 및 DB 회원 탈퇴 처리 기능입니다.")
    public ResponseEntity<Void> deleteUserAccount(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) {

        userService.softDeleteUser(userDetails.getUserId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/online")
    @Operation(summary = "온라인 상태 등록", description = "회원의 온라인 상태를 등록합니다")
    public ResponseEntity notifyUserOnline(@AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        userService.setUserOnline(userDetails.getUserId());

        fcmMessageService.sendScheduledMessages(userDetails.getUserId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/offline")
    @Operation(summary = "오프라인 상태 등록", description = "회원의 오프라인 상태를 등록합니다")
    public ResponseEntity notifyUserOffline(@AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        userService.setUserOffline(userDetails.getUserId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @Operation(summary = "회원 상태 조회", description = "회원이 현재 온라인인지아닌지, 마지막 접속 시간을 조회합니다")
    public ResponseEntity<Map<String,Object>> getUserStatus(@AuthenticationPrincipal CustomUserDetails userDetails){
        Map<String, Object> userStatus = userService.getUserOnlineStatus(userDetails.getUserId());
        return ResponseEntity.ok(userStatus);
    }

    @GetMapping("/partner/{partnerId}")
    @Operation(summary = "궁합 상대 정보 조회", description = "궁합 매칭 상대의 상세 정보를 조회합니다.")
    public ResponseEntity<UserDetailInfoResponse> getUserStatus(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long partnerId){
        UserDetailInfoResponse response = userProfileService.getUserDetailInfo(userDetails.getUserId(), partnerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/notifications")
    @Operation(summary = "받은 알림 목록 조회", description = "특정 유저가 받은 알림 목록을 조회합니다.")
    public ResponseEntity<List<NotificationResponse>> getNotificationList(@AuthenticationPrincipal CustomUserDetails userDetails){
        List<NotificationResponse> response = userNotificationHistoryService.getNotificationList(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/notifications/unread-count")
    @Operation(summary = "읽지 않은 알림 수 조회", description = "특정 유저가 읽지 않은 알림의 개수를 조회합니다.")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails){

        Long unreadCount = userNotificationHistoryService.getUnreadCount(userDetails.getUserId());
        return ResponseEntity.ok(unreadCount);
    }

    @PostMapping("/me/notifications/{notificationId}")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리 합니다.")
    public ResponseEntity<Integer> readNotification(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long notificationId){

        Integer success = userNotificationHistoryService.read(userDetails.getUserId(), notificationId);
        return ResponseEntity.ok(success);
    }
}
