package com.levelup.FaceMeet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.levelup.FaceMeet.domain.User;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserInfoDTO {

    // 회원 정보 변경
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoUpdateRequest{
        private String nickname;
        private String address;
        private Double latitude;
        private Double longitude;
        private Integer preferAgeLower;
        private Integer preferAgeUpper;
    }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  //차단된 사용자 목록 확인
  public static class UserBlockListResponse{

        private Long userId;
        private String nickName;
        private String name;
        private User.Gender gender;
        private LocalDateTime birth;
        private Boolean isDeleted;
        private String img;

        public UserBlockListResponse( User user){

            this.userId = user.getId();
            this.nickName = user.getNickname();
            this.name = user.getName();
            this.gender = user.getGender();
            this.birth = user.getBirth();
            this.isDeleted = user.getIsDeleted();
            this.img = user.getFace() == null ? null : user.getFace().getImg();


        }
  }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoResponse {

        private String name;
        private String email;
        private String nickname;
        private String gender;
        private String address;
        private Double latitude;
        private Double longitude;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime birth;
        private Integer preferAgeLower;
        private Integer preferAgeUpper;
        private Boolean isEventSubscribed;

        public static UserInfoResponse from(User updatedUser) {

            return UserInfoResponse.builder()
                    .name(updatedUser.getName())
                    .email(updatedUser.getEmail())
                    .nickname(updatedUser.getNickname())
                    .gender(updatedUser.getGender() != null ? updatedUser.getGender().name() : User.Gender.u.name())
                    .address(updatedUser.getAddress())
                    .latitude(updatedUser.getLatitude())
                    .longitude(updatedUser.getLongitude())
                    .birth(updatedUser.getBirth())
                    .preferAgeLower(updatedUser.getPreferAgeLower())
                    .preferAgeUpper(updatedUser.getPreferAgeUpper())
                    .build();
        }

        public static UserInfoResponse from(User updatedUser, Boolean isEventSubscribed) {

            return UserInfoResponse.builder()
                    .name(updatedUser.getName())
                    .email(updatedUser.getEmail())
                    .nickname(updatedUser.getNickname())
                    .gender(updatedUser.getGender() != null ? updatedUser.getGender().name() : User.Gender.u.name())
                    .address(updatedUser.getAddress())
                    .latitude(updatedUser.getLatitude())
                    .longitude(updatedUser.getLongitude())
                    .birth(updatedUser.getBirth())
                    .preferAgeLower(updatedUser.getPreferAgeLower())
                    .preferAgeUpper(updatedUser.getPreferAgeUpper())
                    .isEventSubscribed(isEventSubscribed)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoStatusResponse {

        private Boolean hasInfo;
        private Boolean hasFace;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserHomeInfoResponse {

        private String img;
        private String nickname;
        private String title;
        private Integer remainingMatchTickets;
        private Long unreadCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetailInfoResponse {

        private String nickname;
        private Boolean isOnline;
        private LocalDateTime lastSeen;
        private BigDecimal compatibility;
        private Integer age;
        private String img;
        private String title;
        private String description;
        private String faceShapeDesc;
        private String eyeDesc;
        private String eyebrowDesc;
        private String noseDesc;
        private String chinDesc;
        private String mouthDesc;
        private String personality;
        private String careerTraits;
        private String interpersonalRelationships;
        private String lifeDirection;
        private String summaryAnalysis;

        public static UserDetailInfoResponse from(String nickname, Integer age, Boolean isOnline, LocalDateTime lastSeen, BigDecimal compatibility, FaceDTO.FaceInfo faceInfo) {
            return UserDetailInfoResponse.builder()
                    .nickname(nickname)
                    .isOnline(isOnline)
                    .lastSeen(lastSeen)
                    .compatibility(compatibility)
                    .age(age)
                    .img(faceInfo.getImg())
                    .title(faceInfo.getTitle())
                    .description(faceInfo.getDescription())
                    .faceShapeDesc(faceInfo.getDescription())
                    .eyeDesc(faceInfo.getEyeDesc())
                    .eyebrowDesc(faceInfo.getEyebrowDesc())
                    .noseDesc(faceInfo.getNoseDesc())
                    .chinDesc(faceInfo.getChinDesc())
                    .mouthDesc(faceInfo.getMouthDesc())
                    .personality(faceInfo.getPersonality())
                    .careerTraits(faceInfo.getCareerTraits())
                    .interpersonalRelationships(faceInfo.getInterpersonalRelationships())
                    .lifeDirection(faceInfo.getLifeDirection())
                    .summaryAnalysis(faceInfo.getSummaryAnalysis())
                    .build();
        }
    }
}