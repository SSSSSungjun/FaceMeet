package com.levelup.FaceMeet.service.fcm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.*;
import com.levelup.FaceMeet.domain.fcm.FcmToken;
import com.levelup.FaceMeet.domain.fcm.FcmTopic;
import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import com.levelup.FaceMeet.dto.FcmMessageDTO.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.fcm.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmMessageService {

    private final FirebaseMessaging firebaseMessaging;
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmTopicRepository fcmTopicRepository;
    private final ScheduledMessageRepository scheduledMessageRepository;
    private final UserNotificationHistoryService userNotificationHistoryService;
    private final UserNotificationHistoryRepository userNotificationHistoryRepository;
    private final TopicSubscriptionRepository topicSubscriptionRepository;

    @Value("${event.default-topic-name}")
    String defaultTopicName;

    @Value("${event.pre-message-minutes}")
    private int preMessageMinutes;

    /**
     * 모든 특정 토픽 구독자에게 메시지 전송
     */
    public void sendMessageByTopic(FcmTopicMessageRequest request) {
        try {
            FcmTopic topic = fcmTopicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new CustomException(ErrorCode.FCM_TOPIC_NOT_FOUND));

            String title = request.getTitle();
            String body = request.getBody();

            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("title", title)
                    .putData("body", body)
                    .putData("type", "IMMEDIATE_EVENT")
                    .setTopic(topic.getName())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("메시지 전송 성공: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 토픽 메시지 전송 실패. {}", e.getMessage());
            throw new CustomException(ErrorCode.FCM_SEND_FAILED);
        }
    }

    /**
     * 멀티 캐스트 메시지 전송
     */
    private void sendMulticastMessage(List<String> tokens, String title, String body, Map<String, String> data) {
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("title", title)
                    .putData("body", body)
                    .putData("type", "IMMEDIATE_EVENT")
                    .addAllTokens(tokens)
                    .build();

            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            log.debug("멀티캐스트 메시지 전송 완료. 성공: {}, 실패: {}",
                    response.getSuccessCount(), response.getFailureCount());

            // 실패한 토큰들 처리
            handleFailedTokens(tokens, response);

        } catch (FirebaseMessagingException e) {
            log.error("멀티캐스트 메시지 전송 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.FCM_SEND_FAILED);
        }
    }

    /**
     * 멀티 캐스트 메시지 전송 (userId)
     */
    public void sendMessageByUserId(FcmUserMessageRequest request) {
        try {
            List<FcmToken> activeTokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(request.getUserId());

            if (activeTokens.isEmpty()) {
                throw new CustomException(ErrorCode.FCM_TOKEN_NOT_FOUND);
            }

            List<String> tokens = activeTokens.stream()
                    .map(FcmToken::getDeviceToken)
                    .collect(Collectors.toList());

            sendMulticastMessage(tokens, request.getTitle(), request.getBody(), request.getData());

        } catch (Exception  e) {
            log.error("사용자 기반 메시지 전송 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.FCM_SEND_FAILED);
        }
    }

    /**
     * 유니 캐스트 메시지 전송 (token)
     */
    public void sendMessageByToken(FcmTokenMessageRequest request) {
        try {
            String title = request.getTitle();
            String body = request.getBody();

            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("title", title)
                    .putData("body", body)
                    .putData("type", "IMMEDIATE_EVENT")
                    .setToken(request.getToken())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("메시지 전송 성공: {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송 실패. {}", e.getMessage());
            throw new CustomException(ErrorCode.FCM_SEND_FAILED);
        }
    }

    /**
     * 채팅 알림 메시지 전송
     */
    public void sendChatMessageByUserId(String title, String body, Long roomId, Long userId) {
        try {
            List<FcmToken> activeTokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(userId);

            if (activeTokens.isEmpty()) {
                throw new CustomException(ErrorCode.FCM_TOKEN_NOT_FOUND);
            }

            List<String> tokens = activeTokens.stream()
                    .map(FcmToken::getDeviceToken)
                    .collect(Collectors.toList());

                MulticastMessage message = MulticastMessage.builder()
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .putData("type", "CHAT")
                        .putData("roomId", String.valueOf(roomId))
                        .addAllTokens(tokens)
                        .build();

                BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
                log.debug("User {}에게 채팅 알림 전송 완료. 성공: {}, 실패: {}", userId, response.getSuccessCount(), response.getFailureCount());

                handleFailedTokens(tokens, response);

        } catch (CustomException e) {
            log.error("User {}의 디바이스 토큰이 없습니다.: {}", userId, e.getMessage());
        } catch (Exception e) {
            log.error("User {}에게 멀티캐스트 알림 전송 실패: {}", userId, e.getMessage());
//            throw new CustomException(ErrorCode.FCM_SEND_FAILED);
        }
    }

    /**
     * 사전 알림 발송
     */
    @Transactional
    public void sendPreMessage(Long settingId) {
        try {

            FcmTopic topic = fcmTopicRepository.findByName(defaultTopicName)
                    .orElseThrow(() -> new CustomException(ErrorCode.FCM_TOPIC_NOT_FOUND));

            Long topicId = topic.getFcmTopicId();

            String title = "매칭권 이벤트 사전 알림";
            String body = String.format("%d분 뒤에 선착순 매칭권 이벤트가 시작됩니다!", preMessageMinutes);

            Map<String, String> data = new HashMap<>();
            data.put("type", "PRE_MESSAGE");
            data.put("settingId", settingId.toString());
            data.put("title", title);
            data.put("body", body);
            data.put("timestamp", LocalDateTime.now().toString());

            Message message = Message.builder()
                    .setTopic(topic.getName())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            String response = firebaseMessaging.send(message);

            LocalDateTime sentTime = LocalDateTime.now();
            // 사전 메시지를 DB에 저장
            ScheduledMessage scheduledMessage = ScheduledMessage.builder()
                    .settingId(settingId)
                    .fcmTopicId(topicId)
                    .scheduledTime(sentTime)
                    .title(title)
                    .body(body)
                    .messageData(data)
                    .status(ScheduledMessage.MessageStatus.SENT)
                    .build();

            ScheduledMessage savedMessage = scheduledMessageRepository.save(scheduledMessage);
            userNotificationHistoryService.createHistoryForTopicSubscribers(topicId, savedMessage.getId());

            log.info("미리 알림 메시지가 {} 토픽 구독 기기에 전송되었습니다.: {}", topic.getName(), response);
        } catch (Exception e) {
            log.error("미리 알림 메시지 전송 중 에러가 발생하였습니다.", e);
            throw new CustomException(ErrorCode.FCM_PRE_SEND_FAILED);
        }
    }

    /**
     * 사용자에게 현재 예약 상태인 메시지들을 발송
     */
    public void sendScheduledMessages(Long userId) {
        try {

            boolean isSubscribed = topicSubscriptionRepository.existsByUserIdAndFcmTopicId(userId, 1L);
            if (!isSubscribed) {
                return;
            }

            List<FcmToken> activeTokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(userId);

            if (activeTokens.isEmpty()) {
                log.debug("사용자 {}의 등록된 토큰이 존재하지 않습니다.", userId);
                return;
            }

            // 현재 시간 이후에 예약된 활성 메시지들 조회
            List<ScheduledMessage> scheduledMessages = scheduledMessageRepository.findByStatusAndScheduledTimeAfter(
                    ScheduledMessage.MessageStatus.SCHEDULED, LocalDateTime.now());

            if (scheduledMessages.isEmpty()) return;

            List<String> tokens = activeTokens.stream()
                    .map(FcmToken::getDeviceToken)
                    .toList();

            for (ScheduledMessage scheduledMessage : scheduledMessages) {
                sendScheduledMessage(scheduledMessage, userId, tokens);
            }
        } catch (Exception e) {
            log.error("사용자 {}에게 예약 메시지 발송 중 오류 발생: {}", userId, e.getMessage(), e);
            // 실패 시에도 정상 로그인 처리 되도록 예외 X
        }
    }

    public void sendScheduledMessage(ScheduledMessage scheduledMessage, Long userId, List<String> tokens) {
        try {
            boolean alreadySent = userNotificationHistoryRepository.existsByUser_IdAndScheduledMessage_Id(userId, scheduledMessage.getId());
            if(alreadySent) {
                log.debug("이미 알림 {}을 받은 사용자입니다.", scheduledMessage.getId());
                return;
            }

            Map<String, String> data = scheduledMessage.getMessageData();
            data.put("timestamp", LocalDateTime.now().toString());

            MulticastMessage message = MulticastMessage.builder()
                    .putAllData(data)
                    .addAllTokens(tokens)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);

            userNotificationHistoryService.createHistoryForUser(userId, scheduledMessage);

            log.info("예약 메시지가 사용자 {}에게 전송되었습니다. 성공: {}, 실패: {}",
                    userId, response.getSuccessCount(), response.getFailureCount());

            handleFailedTokens(tokens, response);
        } catch (Exception e) {
            log.error("예약 메시지 전송 중 에러가 발생하였습니다.", e);
            throw new CustomException(ErrorCode.FCM_SEND_FAILED, "예약 메시지 전송 중 에러가 발생하였습니다.");
        }
    }

    private void handleFailedTokens(List<String> tokens, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (!sendResponse.isSuccessful()) {
                String token = tokens.get(i);
                FirebaseMessagingException exception = sendResponse.getException();

                if (isTokenInvalidException(exception)) {
                    fcmTokenRepository.findByDeviceToken(token)
                            .ifPresent(t -> {
                                t.setIsActive(false);
                                fcmTokenRepository.save(t);
                            });
                }
            }
        }
    }

    private boolean isTokenInvalidException(FirebaseMessagingException error) {
        MessagingErrorCode errorCode = error.getMessagingErrorCode();
        return errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT;
    }
}
