package com.levelup.FaceMeet.dto;

import com.levelup.FaceMeet.domain.Report;
import lombok.*;

import java.time.LocalDateTime;

public class ReportDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportCreateRequest {
        private Long roomId;
        private Long categoryId;
        private Long reportedId;
        private String reason;
        private String img;
    }

    //관리자가 신고 목록 조회
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportListResponse{

        private Long reportId;

        private Long chatRoomId;

        private Long reportCategoryId;
        private String reportCategoryName;

        //신고자 정보
        private Long reporterId;
        private String reporterName;
        private String reporterNickName;

        //신고 당한 사람 정보
        private Long reportedId;
        private String reportedName;
        private String reportedNickName;

        private String reason;
        private LocalDateTime createdAt;
        private Boolean isSolved;

    }


}