package com.levelup.FaceMeet.service.user;

import com.levelup.FaceMeet.domain.User;
import com.levelup.FaceMeet.dto.UserInfoDTO.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.fcm.TopicSubscriptionRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import com.levelup.FaceMeet.security.service.KakaoService;
import com.levelup.FaceMeet.service.fcm.FcmTopicService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final KakaoService kakaoService;
    private final TopicSubscriptionRepository topicSubscriptionRepository;
    private final FcmTopicService fcmTopicService;

    @Value("${event.default-topic-name}")
    private String defaultTopicName;

    public User findBySocialIdAndProvider(String socialId, String provider) {
        return userRepository.findBySocialIdAndProvider(socialId, provider)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 소셜 회원가입 기본 정보 처리
     */
    public void registerOAuthUser(String socialId, String provider, OAuth2User oAuth2User) {

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String genderAttr = oAuth2User.getAttribute("gender");
        String birthday = oAuth2User.getAttribute("birthday");
        String birthyear = oAuth2User.getAttribute("birthyear");
        User.Gender gender;

        switch (provider) {
            case "kakao":
                gender = "female".equals(genderAttr) ? User.Gender.f : User.Gender.m;
                birthday = birthday.substring(0, 2) + "-" + birthday.substring(2, 4);
                break;
            case "naver":
                gender = "F".equals(genderAttr) ? User.Gender.f : User.Gender.m;
                break;
            default:
                throw new CustomException(ErrorCode.INVALID_PROVIDER);
        }

        String combined = birthyear + "-" + birthday;  // "1990-07-21"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate date = LocalDate.parse(combined, formatter);
        LocalDateTime dateTime = date.atStartOfDay();

        User newUser = User.builder()
                .provider(provider)
                .socialId(socialId)
                .email(email)
                .name(name)
                .gender(gender)
                .birth(dateTime)
                .build();

        userRepository.save(newUser);
    }

    /**
     * 회원 기본 정보 조회
     */
    @Cacheable(value = "user_info", key = "#userId")
    public UserInfoResponse getUserInfo(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Boolean isEventSubscribed = topicSubscriptionRepository.isUserSubscribedToTopic(userId, defaultTopicName);

        return UserInfoResponse.from(user, isEventSubscribed);
    }

    /**
     * 회원 정보 상태 조회
     */
    public UserInfoStatusResponse getUserInfoStatus(Long userId){
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserInfoStatusResponse.builder()
                .hasInfo(user.getNickname() != null)
                .hasFace(user.getFace() != null)
                .build();
    }

    /**
     * 회원 정보 수정
     */
    @Transactional
    @CachePut(value = "user_info", key = "#userId")
    public UserInfoResponse updateUser(Long userId, UserInfoUpdateRequest request) {
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getLatitude() != null) user.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) user.setLongitude(request.getLongitude());
        if (request.getPreferAgeLower() != null) user.setPreferAgeLower(request.getPreferAgeLower());
        if (request.getPreferAgeUpper() != null) user.setPreferAgeUpper(request.getPreferAgeUpper());

        Boolean isEventSubscribed = topicSubscriptionRepository.isUserSubscribedToTopic(userId, defaultTopicName);

        return UserInfoResponse.from(user, isEventSubscribed);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "user_info", key = "#userId"),
        @CacheEvict(value = "refresh_token", key = "#userId"),
        @CacheEvict(value = "user_status", key = "#userId"),
        @CacheEvict(value = "user_face_id", key = "#userId")
    })
    public void softDeleteUser(Long userId) {
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String socialId = user.getSocialId();

        switch (user.getProvider()) {
            case "kakao":
                kakaoService.unlinkUser(socialId);
                break;
            case "naver":
//                naverService.unlinkUser(socialId);
                break;
            default:
                log.error("지원하지 않는 소셜 provider");
        }

        // FCM 서버에서 토픽 직접 구독 해제 처리
        fcmTopicService.unsubscribeAllTopic(userId);
        user.delete();
        userRepository.save(user);
    }

    // 앱에 접속했음을 클라이언트가 알리면 해당 사용자를 레디스에 등록 후 디비에 해당 사용자의 마지막 활동시간을 현재로 변경
    @CachePut(value="user_status", key="#userId")
    public Map<String, Object> setUserOnline(Long userId){

        //db에 마지막 활동 시간 업데이트
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setIsOnline(true);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);

        Map<String, Object> userStatus = new HashMap<>();
        userStatus.put("isOnline", user.getIsOnline());
        userStatus.put("lastSeen", user.getLastSeen());

        return userStatus;
    }

    // 앱에서 해당 사용자가 앱을 종료했음을 알리면 레디스에서 해당 유저를 삭제 후 마지막 활동 시간을 현재로 변경
    @CachePut(value="user_status", key="#userId")
    public Map<String, Object> setUserOffline(Long userId){

        //db에서 마지막 활동 시간 업데이트
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setIsOnline(false);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);

        Map<String, Object> userStatus = new HashMap<>();
        userStatus.put("isOnline", user.getIsOnline());
        userStatus.put("lastSeen", user.getLastSeen());

        return userStatus;
    }

    @Cacheable(value="user_status", key="#userId")
    public Map<String, Object> getUserOnlineStatus(Long userId){

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Map<String, Object> userStatus = new HashMap<>();
        userStatus.put("isOnline", user.getIsOnline());
        userStatus.put("lastSeen", user.getLastSeen());

        return userStatus;
    }
}

