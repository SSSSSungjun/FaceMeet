package com.levelup.FaceMeet.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ChatRoomDTO {

    //새로운 채팅방 저장을 위한 request
    //유저 2명이 서로 매칭되어 새로운 채팅방이 생겼을 db에 새로운 채팅방 생성
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateChatRoomRequest{
        private Long user2Id;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatLeaveRequest{
        private Long userId;
        private Long roomId;
        private Long partnerId;
    }

    //사용자에게 채팅방 목록을 보여줌
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomMemberAndUserAndMessageResponse{
        //보여줘야 할 사용자 정보
        private Long userId;
        private String nickName;
        private LocalDateTime lastActivatedTime;
        private Boolean isOnline;
        private String imgUrl;

        //채팅방 정보
        private Long chatRoomId;
        private String chatRoomStringId;

        //마지막 메시지
        private String lastMessage;
        //마지막 메시지를 보낸 시각
        private LocalDateTime lastSendMessageTime;
        //안읽은 메시지 수
        private Long  nonReadCnt;


        //차단 당한지 여부
        private boolean isBlocked;
        //탈퇴 여부
        private boolean isDeleted;

    }




}
