package com.levelup.FaceMeet.service.user;

import com.levelup.FaceMeet.dto.CacheDTO;
import com.levelup.FaceMeet.dto.FaceDTO.*;
import com.levelup.FaceMeet.dto.UserInfoDTO;
import com.levelup.FaceMeet.dto.UserInfoDTO.*;
import com.levelup.FaceMeet.repository.fcm.UserNotificationHistoryRepository;
import com.levelup.FaceMeet.service.match.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserService userService;
    private final MatchingService matchingService;
    private final FaceService faceService;
    private final UserNotificationHistoryRepository userNotificationHistoryRepository;

    /**
     * 유저 상세 프로필 조회
     */
    public UserDetailInfoResponse getUserDetailInfo(Long requesterId, Long partnerId) {

        UserInfoResponse userInfo = userService.getUserInfo(partnerId);
        Map<String, Object> onlineStatus = userService.getUserOnlineStatus(partnerId);
        BigDecimal compatibility = matchingService.getCompatibility(requesterId, partnerId);
        FaceInfo faceInfo = getFaceInfoByUserId(partnerId);

        String nickname = userInfo.getNickname();
        Boolean isOnline = (Boolean) onlineStatus.get("isOnline");
        LocalDateTime lastSeen = (LocalDateTime) onlineStatus.get("lastSeen");

        int age = calculateAge(userInfo.getBirth().toLocalDate());

        return UserDetailInfoResponse.from(nickname, age, isOnline, lastSeen, compatibility, faceInfo);
    }

    /**
     * 유저 홈 화면 정보 조회
     */
    public UserHomeInfoResponse getUserHomeInfo(Long userId) {

        UserInfoResponse user = userService.getUserInfo(userId);
        FaceInfo face = getFaceInfoByUserId(userId);
        Integer remainingMatchTickets = matchingService.getRemainMathchingCnt(userId);
        Long unreadCount = userNotificationHistoryRepository.countByUser_IdAndIsReadFalseAndReceivedAtBefore(userId, LocalDateTime.now());

        return UserInfoDTO.UserHomeInfoResponse.builder()
                .img(face.getImg())
                .nickname(user.getNickname())
                .title(face.getTitle())
                .remainingMatchTickets(remainingMatchTickets)
                .unreadCount(unreadCount)
                .build();
    }

    /**
     * 유저 관상 정보 조회
     */
    public UserFaceInfoResponse getUserFaceInfo(Long userId) {

        UserInfoResponse user = userService.getUserInfo(userId);
        FaceInfo faceInfo = getFaceInfoByUserId(userId);

        return UserFaceInfoResponse.builder()
                .faceId(faceInfo.getFaceId())
                .name(user.getName())
                .nickname(user.getNickname())
                .img(faceInfo.getImg())
                .title(faceInfo.getTitle())
                .description(faceInfo.getDescription())
                .faceShapeDesc(faceInfo.getFaceShapeDesc())
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

    private FaceInfo getFaceInfoByUserId(Long userId) {
        CacheDTO.CachedLong userFaceId = faceService.getUserFaceId(userId);
        return faceService.getFaceInfo(userFaceId.getData());
    }

    private int calculateAge(LocalDate birthDate) {
        LocalDate today = LocalDate.now();
        int age = today.getYear() - birthDate.getYear();
        if (today.isBefore(birthDate.withYear(today.getYear()))) {
            age--;
        }
        return age;
    }
}
