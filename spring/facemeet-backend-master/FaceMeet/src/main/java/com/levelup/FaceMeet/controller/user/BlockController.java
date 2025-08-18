package com.levelup.FaceMeet.controller.user;

import com.levelup.FaceMeet.dto.UserInfoDTO;
import com.levelup.FaceMeet.security.dto.CustomUserDetails;
import com.levelup.FaceMeet.service.user.UserBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/blocks")
@RequiredArgsConstructor
@Tag(name = "BlockController", description = "유저간 차단 기능 제공")
public class BlockController {

    private final UserBlockService userBlockService;

    @PostMapping("/{userId}")
    @Operation(summary = "대상 차단", description = "특정 유저를 차단하는 기능입니다.")
    public ResponseEntity<?> blockUser(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long userId) {

        Long blockerId = userDetails.getUserId();

        userBlockService.createBlock(blockerId, userId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("")
    @Operation(summary = "차단 목록 확인", description = "회원이 직접 차단한 사용자 목록을 확인합니다")
    public ResponseEntity<List<UserInfoDTO.UserBlockListResponse>> blockUser(@AuthenticationPrincipal CustomUserDetails userDetails) {

        List<UserInfoDTO.UserBlockListResponse> blockLists = userBlockService.getUserBlockList(userDetails.getUserId());

        return ResponseEntity.ok(blockLists);
    }

    @DeleteMapping("/{blockedId}")
    @Operation(summary = "차단 해제", description = "특정 사용자의 차단을 해제합니다.")
    public ResponseEntity<Void> unblockUser(@PathVariable Long blockedId,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        userBlockService.unblockUser(userDetails.getUserId(), blockedId);

        return ResponseEntity.noContent().build();
    }
}