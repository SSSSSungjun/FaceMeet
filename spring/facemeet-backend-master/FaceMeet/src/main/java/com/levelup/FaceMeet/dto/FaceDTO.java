package com.levelup.FaceMeet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class FaceDTO {

    // 회원 정보 변경
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FaceInfo {
        private Long faceId;
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
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserFaceInfoResponse {
        private Long faceId;
        private String name;
        private String nickname;
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
    }
}
