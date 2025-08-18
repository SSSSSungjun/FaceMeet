package com.levelup.FaceMeet.controller.match;

import com.levelup.FaceMeet.dto.MatchDTO;
import com.levelup.FaceMeet.security.dto.CustomUserDetails;
import com.levelup.FaceMeet.service.match.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
@Tag(name = "MatchController", description = "매칭 관련 기능" )
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping("/remain")
    @Operation(summary = "잔여 매칭권수 확인", description = "잔여 매칭 권수를 확인합니다")
    public ResponseEntity<Integer> requiredTicket(@AuthenticationPrincipal CustomUserDetails customUserDetails ) {

        int cnt = matchingService.getRemainMathchingCnt(customUserDetails.getUserId());

        return ResponseEntity.ok(cnt);
    }

    @GetMapping("/")
    @Operation(summary = "매칭", description = "매칭 결과로 채팅방 번호만 반환합니다")
    public MatchDTO.MathchingSucessResponseChatRoomId getMathcingResult(@AuthenticationPrincipal CustomUserDetails customUserDetails){

        return matchingService.getChatRoomId(customUserDetails.getUserId());
    }

}
