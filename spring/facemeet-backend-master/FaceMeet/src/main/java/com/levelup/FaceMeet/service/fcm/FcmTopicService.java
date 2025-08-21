package com.levelup.FaceMeet.service.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import com.levelup.FaceMeet.common.FcmTokenHandler;
import com.levelup.FaceMeet.domain.fcm.FcmToken;
import com.levelup.FaceMeet.domain.fcm.FcmTopic;
import com.levelup.FaceMeet.domain.fcm.TopicSubscription;
import com.levelup.FaceMeet.dto.FcmMessageDTO.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.fcm.FcmTokenRepository;
import com.levelup.FaceMeet.repository.fcm.FcmTopicRepository;
import com.levelup.FaceMeet.repository.fcm.TopicSubscriptionRepository;
import com.levelup.FaceMeet.service.fcm.messaging.ScheduledMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTopicService {

    private final FcmTokenRepository fcmTokenRepository;
    private final FirebaseMessaging firebaseMessaging;
    private final FcmTopicRepository fcmTopicRepository;
    private final FcmTokenHandler fcmTokenHandler;
    private final TopicSubscriptionRepository topicSubscriptionRepository;
    private final CacheManager cacheManager;

    private final ScheduledMessagingService scheduledMessagingService;

    @Value("${event.default-topic-name}")
    private String defaultTopicName;

    /**
     * 토픽 생성
     */
    @Transactional
    public void createTopic(FcmTopicUpsertRequest request) {
        log.info("FCM 토픽 생성 요청: {}", request.getName());

        fcmTopicRepository.findByName(request.getName())
                .filter(FcmTopic::getIsActive)
                .ifPresent(topic -> {
                    throw new CustomException(ErrorCode.DUPLICATE_TOPIC_NAME);
                });

        // 토픽 생성
        FcmTopic topic = FcmTopic.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .build();

        FcmTopic savedTopic = fcmTopicRepository.save(topic);
        log.info("FCM 토픽 생성 완료: ID={}, Name={}", savedTopic.getFcmTopicId(), savedTopic.getName());
    }

    /**
     * 토픽 삭제
     */
    @Transactional
    public void deleteTopic(Long topicId) {
        FcmTopic topic = fcmTopicRepository.findByFcmTopicId(topicId)
                .orElseThrow(() -> new CustomException(ErrorCode.FCM_TOPIC_NOT_FOUND));

        // 토픽을 구독하는 모든 활성 토큰을 한 번에 조회
        List<String> allTokensToUnsubscribe = topicSubscriptionRepository.findAllTokensByTopicId(topicId);

        // 일괄 구독 해제
        if (!allTokensToUnsubscribe.isEmpty()) {
            try {
                firebaseMessaging.unsubscribeFromTopic(allTokensToUnsubscribe, topic.getName());
                log.info("토픽 '{}'에서 {}개의 토큰 일괄 구독 해제 성공", topic.getName(), allTokensToUnsubscribe.size());
            } catch (FirebaseMessagingException e) {
                log.error("토픽 '{}' 일괄 구독 해제 실패: {}", topic.getName(), e.getMessage());
                throw new CustomException(ErrorCode.TOPIC_SUBSCRIPTION_FAILED);
            }
        }

        // DB에서 모든 구독 정보 삭제
        topicSubscriptionRepository.deleteAllByFcmTopicId(topicId);

        // 토픽 비활성화
        topic.setIsActive(false);
    }

    /**
     * 토픽 구독
     */
    @Transactional
    public FcmTopicSubscriptionResponse subscribeTopic(Long userId, Long topicId) {
        FcmTopic topic = fcmTopicRepository.findById(topicId)
                .filter(FcmTopic::getIsActive)
                .orElseThrow(() -> new CustomException(ErrorCode.FCM_TOPIC_NOT_FOUND));

        // 기존 구독 여부 확인
        topicSubscriptionRepository.findByUserIdAndFcmTopicId(userId, topicId)
                .ifPresent(subscription -> {
                    throw new CustomException(ErrorCode.ALREADY_SUBSCRIBED_TOPIC);
                });

        TopicOperationResult result = executeTopicOperation(userId, topic.getName(), TopicOperation.SUBSCRIBE);

        // DB에 구독 정보 저장 (성공한 경우만)
        topicSubscriptionRepository.save(
                TopicSubscription.builder()
                        .userId(userId)
                        .fcmTopicId(topicId)
                        .build()
        );

        // event 토픽인 경우 해당 유저의 캐시 삭제
        if (defaultTopicName.equals(topic.getName())) {
            evictUserInfoCache(userId);
            scheduledMessagingService.sendScheduledMessagesToUser(userId);
        }

        return buildResponse(userId, topicId, result);
    }

    /**
     * 토픽 구독 취소
     */
    @Transactional
    public FcmTopicSubscriptionResponse unsubscribeTopic(Long userId, Long topicId) {
        FcmTopic topic = fcmTopicRepository.findById(topicId)
                .orElseThrow(() -> new CustomException(ErrorCode.FCM_TOPIC_NOT_FOUND));

        // 구독 여부 확인
        topicSubscriptionRepository.findByUserIdAndFcmTopicId(userId, topicId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_SUBSCRIBED_TOPIC));

        TopicOperationResult result = executeTopicOperation(userId, topic.getName(), TopicOperation.UNSUBSCRIBE);

        // DB에서 구독 정보 삭제
        topicSubscriptionRepository.deleteByUserIdAndFcmTopicId(userId, topicId);

        // event 토픽인 경우 해당 유저의 캐시 삭제
        if (defaultTopicName.equals(topic.getName())) {
            evictUserInfoCache(userId);
        }

        return buildResponse(userId, topicId, result);
    }

    /**
     * 유저 구독 토픽 전체 취소 (회원 탈퇴용)
     */
    @Transactional
    public void unsubscribeAllTopic(Long userId) {

        List<TopicSubscription> topics = topicSubscriptionRepository.findByUserId(userId);

        for(TopicSubscription topic : topics) {
            executeTopicOperation(userId, topic.getFcmTopic().getName(), TopicOperation.UNSUBSCRIBE);
        }

        // DB에서 구독 정보 삭제
        topicSubscriptionRepository.deleteByUserId(userId);
    }

    // 응답 생성
    private FcmTopicSubscriptionResponse buildResponse(Long userId, Long topicId, TopicOperationResult result) {
        return FcmTopicSubscriptionResponse.builder()
                .topicId(topicId)
                .userId(userId)
                .subscribedDeviceCount(result.successTokens().size())
                .subscribedTokens(result.successTokens())
                .failedTokens(result.failedTokens())
                .build();
    }

    /**
     * 토픽 구독/구독해제 공통 실행 로직
     */
    private enum TopicOperation {
        SUBSCRIBE, UNSUBSCRIBE
    }

    private record TopicOperationResult(List<String> successTokens, List<String> failedTokens) {}

    private TopicOperationResult executeTopicOperation(Long userId, String topicName, TopicOperation operation) {
        List<String> tokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(FcmToken::getDeviceToken)
                .collect(Collectors.toList());

        List<String> successTokens = new ArrayList<>();
        List<String> failedTokens = new ArrayList<>();

        if (tokens.isEmpty()) {
            log.warn("사용자 {}의 활성 FCM 토큰이 없습니다.", userId);
            return new TopicOperationResult(successTokens, failedTokens);
        }

        try {
            TopicManagementResponse response = switch (operation) {
                case SUBSCRIBE -> firebaseMessaging.subscribeToTopic(tokens, topicName);
                case UNSUBSCRIBE -> firebaseMessaging.unsubscribeFromTopic(tokens, topicName);
            };

            // 성공/실패 분류
            classifyTokenResults(tokens, response, successTokens, failedTokens, operation);

        } catch (FirebaseMessagingException e) {
            String operationName = operation == TopicOperation.SUBSCRIBE ? "구독" : "구독 해제";
            log.error("토픽 {} 실패: {}", operationName, e.getMessage());
            throw new CustomException(ErrorCode.TOPIC_SUBSCRIPTION_FAILED);
        }

        return new TopicOperationResult(successTokens, failedTokens);
    }

    /**
     * Firebase 응답에서 성공/실패 토큰 분류
     */
    private void classifyTokenResults(List<String> tokens,
                                      TopicManagementResponse response,
                                      List<String> successTokens,
                                      List<String> failedTokens,
                                      TopicOperation operation) {

        int successCount = response.getSuccessCount();
        int failureCount = response.getFailureCount();

        log.info("토픽 {} 결과: 성공 {}, 실패 {}",
                operation == TopicOperation.SUBSCRIBE ? "구독" : "구독해제",
                successCount, failureCount);

        for (int i = 0; i < tokens.size(); i++) {
            if (i < response.getErrors().size() && response.getErrors().get(i) != null) {
                String token = tokens.get(i);
                failedTokens.add(token);

                String errorMessage = response.getErrors().get(i).getReason();
                log.warn("토큰 {}에서 오류 발생: {}", token, errorMessage);

                if (operation == TopicOperation.SUBSCRIBE) {
                    fcmTokenHandler.handleFailedToken(token, response.getErrors().get(i));
                }
            } else {
                successTokens.add(tokens.get(i));
            }
        }
    }


    /**
     * 사용자 구독 목록 조회
     */
    public UserSubscriptionResponse getUserSubscriptions(Long userId) {
        List<String> topicNames = topicSubscriptionRepository.findByUserIdWithTopic(userId)
                .stream()
                .filter(sub -> sub.getFcmTopic().getIsActive())     // 활성화 된 토픽만
                .map(sub -> sub.getFcmTopic().getName())
                .collect(Collectors.toList());

        return UserSubscriptionResponse.builder().userId(userId).topicName(topicNames).build();
    }

    /**
     * 단일 유저 캐시 삭제
     */
    private void evictUserInfoCache(Long userId) {
        Cache cache = cacheManager.getCache("user_info");
        if (cache != null) {
            cache.evict(userId);
            log.debug("user_info 캐시 삭제: userId={}", userId);
        }
    }
}
