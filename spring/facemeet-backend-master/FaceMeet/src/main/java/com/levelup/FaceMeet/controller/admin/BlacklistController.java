package com.levelup.FaceMeet.controller.admin;

import com.levelup.FaceMeet.domain.Blacklist;
import com.levelup.FaceMeet.domain.BlacklistCategory;
import com.levelup.FaceMeet.dto.BlacklistDTO;
import com.levelup.FaceMeet.dto.BlacklistDTO.*;
import com.levelup.FaceMeet.dto.ChatRoomDTO;
import com.levelup.FaceMeet.security.dto.CustomUserDetails;
import com.levelup.FaceMeet.service.admin.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/blacklist")
@RequiredArgsConstructor
@Tag(name = "BlacklistController", description = "블랙리스트 기능 제공")
public class BlacklistController {

    private final BlacklistService blacklistService;

    @PostMapping("/")
    @Operation(summary = "블랙리스트 생성", description = "특정 유저를 블랙리스트에 추가하여 접근을 금지 시키는 기능입니다.")
    public ResponseEntity<?> addBlacklist(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid BlacklistRequest request) {

        Long adminId = userDetails.getUserId();

        blacklistService.createReport(adminId, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/")
    @Operation(summary = "블랙리스트 목록 조회" , description = "관리자가 전체 블랙리스트 목록을 조회합니다")
    public ResponseEntity<List<BlacklistDTO.BlackListResponse>> getBlackList(){
        List<BlacklistDTO.BlackListResponse> list = blacklistService.getBlackList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{blacklistid}")
    @Operation(summary = "블랙리스트 삭제", description = "관리자가 전체 블랙리스트를 삭제합니다")
    public ResponseEntity<Void> deleteBlackList(@PathVariable Long blacklistid) {
        blacklistService.deleteBlackList(blacklistid);
        return ResponseEntity.noContent().build();  // 204 No Content 반환
    }

    @GetMapping("/category")
    @Operation(summary = "블랙리스트 카테고리 목록 조회" , description = "블랙리스트의 전체 카테고리 목록을 조회합니다")
    public ResponseEntity<List<BlacklistCategory>> getBlackListCategoryList(){
        List<BlacklistCategory>  categories = blacklistService.getBlacklistCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/admin/users/{userId}/chat-rooms")
    @Operation(summary = "유저별 채팅방 목록 조회", description = "특정 유저가 참여한 채팅방 목록을 조회합니다.")
    public ResponseEntity<List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse>> getUserChatRoomList(
            @PathVariable("userId") Long userId
    ) {
        List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse> chatRooms = blacklistService.getUserChatRoomList(userId);
        return ResponseEntity.ok(chatRooms);
    }
}
