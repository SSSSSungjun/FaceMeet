package com.levelup.FaceMeet.service.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.levelup.FaceMeet.domain.fcm.FcmToken;
import com.levelup.FaceMeet.domain.fcm.TopicSubscription;
import com.levelup.FaceMeet.dto.FcmMessageDTO.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.fcm.FcmTokenRepository;
import com.levelup.FaceMeet.repository.fcm.TopicSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;
    private final FirebaseMessaging  firebaseMessaging;
    private final TopicSubscriptionRepository topicSubscriptionRepository;

    /**
     * 토큰 등록/갱신
     */
    public FcmTokenResponse upsertToken(Long userId, FcmTokenUpsertRequest request) {

        // 이전 토큰 처리
//        if (request.getPreviousToken() != null) {
//            deactivateToken(request.getPreviousToken());
//        }
        FcmToken token = null;
        boolean isNew = false;

        // deviceId가 있는 경우, 같은 deviceId를 가진 기존 토큰 찾기
        if (request.getDeviceId() != null) {
//            deactivateOtherTokensForDevice(userId, request.getDeviceId(), request.getDeviceToken());

            Optional<FcmToken> existingTokenByDevice = fcmTokenRepository
                    .findActiveTokenByUserIdAndDeviceId(userId, request.getDeviceId());

            if (existingTokenByDevice.isPresent()) {
                // 같은 deviceId를 가진 토큰이 있으면 토큰 값만 업데이트
                token = existingTokenByDevice.get();
            }
        }

        // deviceId로 찾지 못했다면 토큰 값으로 찾기
        if (token == null) {
            token = fcmTokenRepository.findByDeviceToken(request.getDeviceToken())
                    .orElse(null);
        }

        // 기존 토큰이 없다면 새로 생성
        if (token == null) {
            token = FcmToken.builder()
                    .deviceToken(request.getDeviceToken())
                    .build();
            isNew = true;
        }

        token.setUserId(userId);
        token.setDeviceType(request.getDeviceType());
        token.setDeviceToken(request.getDeviceToken());
        token.setDeviceId(request.getDeviceId());
        token.setLastUsedAt(LocalDateTime.now());
        token.setIsActive(true);

        FcmToken savedToken = fcmTokenRepository.save(token);

        List<TopicSubscription> topicSubscriptions = topicSubscriptionRepository.findByUserId(userId);
        List<String> tokens = new ArrayList<>();
        tokens.add(savedToken.getDeviceToken());

        for (TopicSubscription sub : topicSubscriptions) {
            try {
                firebaseMessaging.subscribeToTopic(tokens, sub.getFcmTopic().getName());
            } catch (FirebaseMessagingException e) {
                throw new CustomException(ErrorCode.TOPIC_SUBSCRIPTION_FAILED, String.format("토픽 구독 처리 중 오류가 발생하였습니다. 토큰: [%s]", savedToken.getDeviceToken()));
            }
        }

        return FcmTokenResponse.builder()
                .tokenId(savedToken.getFcmTokenId())
                .deviceToken(savedToken.getDeviceToken())
                .deviceType(savedToken.getDeviceType())
                .isNew(isNew)
                .lastUsedAt(savedToken.getLastUsedAt())
                .build();
    }

    /**
     * 토큰 삭제
     */
    public void deleteToken(Long userId, String deviceToken) {
        fcmTokenRepository.findByDeviceToken(deviceToken)
                .filter(token -> token.getUserId().equals(userId))
                .ifPresentOrElse(
                        token -> {
                            token.setIsActive(false);
                            fcmTokenRepository.save(token);
                        },
                        () -> {
                            throw new CustomException(ErrorCode.FCM_TOKEN_NOT_FOUND);
                        }
                );
    }

    /**
     * 사용자 토큰 목록 조회
     */
    public List<FcmTokenResponse> getUserTokens(Long userId) {
        return fcmTokenRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(token -> FcmTokenResponse.builder()
                        .tokenId(token.getFcmTokenId())
                        .deviceToken(token.getDeviceToken()) // 마스킹 여부는 아래 참고
                        .deviceType(token.getDeviceType())
                        .isNew(false) // 기존 토큰이므로
                        .lastUsedAt(token.getLastUsedAt())
                        .build())
                .collect(Collectors.toList());
    }


    // 기타 유틸리티 메서드 ==============================
    private void deactivateToken(String deviceToken) {
        fcmTokenRepository.findByDeviceToken(deviceToken)
                .ifPresent(token -> {
                    token.setIsActive(false);
                    fcmTokenRepository.save(token);
                });
    }

    private void deactivateOtherTokensForDevice(Long userId, String deviceId, String currentToken) {
        List<FcmToken> otherTokens = fcmTokenRepository.findActiveTokensByUserIdAndDeviceIdExcluding(
                userId, deviceId, currentToken);

        otherTokens.forEach(token -> {
            token.setIsActive(false);
        });

        fcmTokenRepository.saveAll(otherTokens);
    }
}