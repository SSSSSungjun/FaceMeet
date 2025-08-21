package com.levelup.FaceMeet.service.fcm.messaging;

import com.levelup.FaceMeet.config.fcm.FcmConstants;
import com.levelup.FaceMeet.domain.fcm.FcmToken;
import com.levelup.FaceMeet.domain.fcm.FcmTopic;
import com.levelup.FaceMeet.dto.FcmMessageDTO.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.fcm.FcmTokenRepository;
import com.levelup.FaceMeet.repository.fcm.FcmTopicRepository;
import com.levelup.FaceMeet.service.fcm.ScheduledMessageService;
import com.levelup.FaceMeet.service.fcm.builder.FcmMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FCM 메시지 전송 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImmediateMessagingService {

    private final FcmTokenRepository fcmTokenRepository;
    private final FcmTopicRepository fcmTopicRepository;

    private final FcmAsyncService fcmAsyncService;

    private final FcmMessageBuilder messageBuilder;

    private final ScheduledMessageService scheduledMessageService;

    /**
     * 토픽으로 메시지 전송
     */
    public void sendMessageByTopic(FcmTopicMessageRequest request) {
        FcmTopic topic = fcmTopicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new CustomException(ErrorCode.FCM_TOPIC_NOT_FOUND));

        Map<String, String> data = messageBuilder.buildBasicData(
                request.getTitle(),
                request.getBody(),
                request.getData(),
                FcmConstants.MessageType.IMMEDIATE_EVENT
        );

        fcmAsyncService.sendTopicMessageAsync(topic.getName(), request.getTitle(), request.getBody(), data);

        log.info("토픽 메시지 전송 완료: topicId={}, topic={}", request.getTopicId(), topic.getName());
    }

    /**
     * 토큰으로 메시지 전송
     */
    public void sendMessageByToken(FcmTokenMessageRequest request) {
        Map<String, String> data = messageBuilder.buildBasicData(
                request.getTitle(),
                request.getBody(),
                request.getData(),
                FcmConstants.MessageType.IMMEDIATE_EVENT
        );

        fcmAsyncService.sendMulticastMessageAsync(List.of(request.getToken()), request.getTitle(), request.getBody(), data);

        log.info("토큰 직접 전송 완료");
    }

    /**
     * 사용자 ID로 메시지 전송
     */
    public void sendMessageByUserId(FcmUserMessageRequest request) {
        List<String> tokens = getActiveTokens(request.getUserId());

        if (tokens.isEmpty()) {
            log.warn("사용자 {}의 활성 토큰이 없습니다.", request.getUserId());
            throw new CustomException(ErrorCode.FCM_TOKEN_NOT_FOUND);
        }

        Map<String, String> data = messageBuilder.buildBasicData(
                request.getTitle(),
                request.getBody(),
                request.getData(),
                FcmConstants.MessageType.IMMEDIATE_EVENT
        );

        fcmAsyncService.sendMulticastMessageAsync(tokens, request.getTitle(), request.getBody(), data);

        log.info("사용자 {} 메시지 전송 완료. 토큰 수: {}", request.getUserId(), tokens.size());
    }

    /**
     * 채팅 메시지 전송
     */
    public void sendChatMessageByUserId(String title, String body, Long roomId, Long userId) {

        List<String> tokens = getActiveTokens(userId);

        fcmAsyncService.sendChatMessageAsync(tokens, title, body, roomId, userId);
    }

    /**
     * 사용자의 활성 토큰 조회
     */
    private List<String> getActiveTokens(Long userId) {
        return fcmTokenRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(FcmToken::getDeviceToken)
                .collect(Collectors.toList());
    }
}