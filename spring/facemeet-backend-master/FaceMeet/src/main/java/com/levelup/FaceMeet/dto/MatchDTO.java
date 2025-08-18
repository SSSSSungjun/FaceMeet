package com.levelup.FaceMeet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MatchDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchResponse {
        @JsonProperty("match_user_id")
        private Long matchUserId;

        private double similarity;


    }

    //매칭 됬을 경우 채팅방 아이디만 반환합니다
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MathchingSucessResponseChatRoomId{

        //채팅방 아이디
        private Long chatRoomId;


    }

    //매칭 됬을 경우 반환할 채팅 정보
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MathchingSucessResponse{

        //채팅방 아이디
        private Long chatRoomId;

        //상대방 프로필 아이디
        private Long partnerId;
        private String partnerNickname;
        private Boolean blocked;
        private Boolean deleted;

        //상대방 관상 이미지 url
        private String imgUrl;

        //궁합도
        private Double similar;



    }
}
