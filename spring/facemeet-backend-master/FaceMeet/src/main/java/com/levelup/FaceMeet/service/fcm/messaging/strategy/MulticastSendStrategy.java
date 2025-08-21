package com.levelup.FaceMeet.service.fcm.messaging.strategy;

import com.google.firebase.messaging.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.service.fcm.TokenFailureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.levelup.FaceMeet.config.fcm.FcmConstants;

import java.util.List;
import java.util.Map;

/**
 * 멀티캐스트 전송 전략
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MulticastSendStrategy {// FCM 제한

    private final FirebaseMessaging firebaseMessaging;
    private final TokenFailureService tokenFailureService;

    public void send(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens.isEmpty()) {
            log.warn("전송할 토큰이 없습니다.");
            return;
        }

        // 500개씩 배치 처리
        for (int i = 0; i < tokens.size(); i += FcmConstants.MAX_MULTICAST_SIZE) {
            List<String> batch = tokens.subList(i,
                    Math.min(i + FcmConstants.MAX_MULTICAST_SIZE, tokens.size()));
            sendBatch(batch, title, body, data);
        }
    }

    private void sendBatch(List<String> tokens, String title, String body, Map<String, String> data) {
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .addAllTokens(tokens)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);

            log.info("멀티캐스트 전송 결과 - 성공: {}, 실패: {}",
                    response.getSuccessCount(), response.getFailureCount());

            // 실패한 토큰 처리
            if (response.getFailureCount() > 0) {
                tokenFailureService.handleFailedTokens(tokens, response);
            }

        } catch (FirebaseMessagingException e) {
            log.error("멀티캐스트 전송 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.FCM_SEND_FAILED);
        }
    }
}