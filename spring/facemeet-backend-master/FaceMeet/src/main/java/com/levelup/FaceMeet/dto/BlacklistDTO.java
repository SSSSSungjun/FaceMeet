package com.levelup.FaceMeet.dto;

import lombok.*;

import java.time.LocalDateTime;

public class BlacklistDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlacklistRequest {
        private Long reportId;
        private Long categoryId;
    }

    //블랙리스트 목록 조회
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BlackListResponse{

      private Long blacklistId;

      private Long userId;
      private String userName;
      private String provider;

      private Long blackListCategoryId;
      private String blackListCategoryName;

      private LocalDateTime createdAt;

    }
}
