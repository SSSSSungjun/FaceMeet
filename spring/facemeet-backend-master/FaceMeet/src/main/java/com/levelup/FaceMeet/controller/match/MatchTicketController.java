package com.levelup.FaceMeet.controller.match;

import com.levelup.FaceMeet.dto.MatchTicketDTO;
import com.levelup.FaceMeet.security.dto.CustomUserDetails;
import com.levelup.FaceMeet.service.match.MatchTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
@Tag(name = "MatchTicketController", description = "매칭 티켓을 선착순으로 획득하는 동시성 관련 기능")

public class MatchTicketController {

    @Autowired
    private MatchTicketService matchTicketService;

    @PostMapping("/{settingid}")
    @Operation(summary = "매칭 티켓 선착순 획득", description = "매칭 티켓을 선착순으로 획득합니다")
    public ResponseEntity<MatchTicketDTO.MatchTicketResponse> requiredTicket(@AuthenticationPrincipal CustomUserDetails customUserDetails , @PathVariable Long settingid) {

        MatchTicketDTO.MatchTicketResponse response = matchTicketService.acquireTicketWithRedisLock(customUserDetails.getUserId(),settingid );

        return ResponseEntity.ok(response);
    }


}
