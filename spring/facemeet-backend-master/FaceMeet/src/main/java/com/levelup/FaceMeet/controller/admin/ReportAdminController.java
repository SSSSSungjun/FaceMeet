package com.levelup.FaceMeet.controller.admin;

import com.levelup.FaceMeet.dto.ReportDTO;
import com.levelup.FaceMeet.service.user.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@Tag(name = "ReportController", description = "관리자 신고 기능 제공")
public class ReportAdminController {

    private final ReportService reportService;

    @GetMapping("/")
    @Operation(summary = "신고 목록 조회", description = "관리자가 전체 신고 목록을 조회합니다")
    public ResponseEntity<List<ReportDTO.ReportListResponse>> reportUser() {

        List<ReportDTO.ReportListResponse> reportsDTOS = reportService.getReportList();

        return ResponseEntity.ok(reportsDTOS);
    }

    @PostMapping("/{reportId}")
    @Operation(
            summary = "신고 처리 완료",
            description = "관리자가 신고 처리를 완료합니다. 블랙리스트 등록 시에는 별도로 추가 작업이 필요합니다."
    )
    public ResponseEntity<Void> resolveReport(@PathVariable Long reportId) {
        reportService.resolveReport(reportId);
        return ResponseEntity.noContent().build();
    }

}
