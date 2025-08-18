package com.levelup.FaceMeet.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

public class MessageDTO {

    //메시지 저장을 위한 request
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageSaveRequest{
        private Long roomId;
        private Long senderId;
        private Long receiverId;
        private String content;

    }

    //채팅방 대화 내역 조회@Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class AllMessageResponse {

        private PagedMessagesResponse messages;
        private MatchDTO.MathchingSucessResponse chatRoom;

    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    //채팅 시 수신자에게 온 메시지를 보냄
    public static class MessageSendResponse {
        private String type;

        private String content;
        private Long senderId;
        private Long receiverId;
        private Long roomId;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime sendAt;
        private Boolean isRead;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime readAt;

        public MessageSendResponse(String content, Long senderId, Long receiverId, Long roomId, LocalDateTime sendAt,Boolean isRead, LocalDateTime readAt) {
            this.type = "MESSAGE";
            this.content = content;
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.roomId = roomId;
            this.sendAt = sendAt;
            this.isRead = isRead;
            this.readAt = readAt;
        }


    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PagedMessagesResponse {
        private List<MessageSendResponse> messages;
        private long currentPage;
        private long totalPages;

    }


    //메시지 읽음 처리를 위한 request
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageReadRequest{
        private Long readerId;
        private Long senderId;
        private Long roomId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatSummaryResponse {

        private String lastMessage;
        private LocalDateTime lastMessageTime;
        private long totalUnreadCountFromPartner;

    }

}
