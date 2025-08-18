package com.levelup.FaceMeet.controller.user;

import com.levelup.FaceMeet.domain.ReportCategory;
import com.levelup.FaceMeet.dto.ReportDTO.*;
import com.levelup.FaceMeet.security.dto.CustomUserDetails;
import com.levelup.FaceMeet.service.user.ReportService;
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
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "ReportController", description = "신고 기능 제공")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/")
    @Operation(summary = "신고", description = "특정 유저를 신고하는 기능입니다.")
    public ResponseEntity<?> reportUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid ReportCreateRequest reportCreateRequest) {

        Long reporterId = userDetails.getUserId();

        reportService.createReport(reporterId, reportCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/category")
    @Operation(summary = "신고 카테고리 목록 조회" , description = "신고 카테고리의 모든 목록을 조회합니다")
    public ResponseEntity<List<ReportCategory>> getReportCategory() {

        List<ReportCategory> categories = reportService.getReportCategoryList();

        return ResponseEntity.ok(categories);
    }

}