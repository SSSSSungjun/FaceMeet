package com.levelup.FaceMeet.dto;

import lombok.*;

import java.time.LocalDateTime;

public class MatchTicketDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MatchTicketResponse  {
        private boolean sucess;
        private String message;
        private Long settingId;
        private Long userId;
        private LocalDateTime acquiredAt;
        private Integer remainingCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TicketStatusResponse{
        private Long ticketId;
        private Integer maxCount;
        private Long remainingCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean isActive;
    }


}
