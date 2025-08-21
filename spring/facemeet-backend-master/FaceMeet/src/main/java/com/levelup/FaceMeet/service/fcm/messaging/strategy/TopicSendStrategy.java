package com.levelup.FaceMeet.service.fcm.messaging.strategy;

import com.google.firebase.messaging.*;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 토픽 전송 전략
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TopicSendStrategy {

    private final FirebaseMessaging firebaseMessaging;

    public void send(String topicName, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setNotification(createNotification(title, body))
                    .putAllData(data)
                    .setTopic(topicName)
                    .setAndroidConfig(getAndroidConfig())
                    .setApnsConfig(getApnsConfig())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("토픽 메시지 전송 성공: topic={}, messageId={}", topicName, response);

        } catch (FirebaseMessagingException e) {
            log.error("토픽 메시지 전송 실패: topic={}, error={}", topicName, e.getMessage());
            throw new CustomException(ErrorCode.FCM_SEND_FAILED);
        }
    }

    private Notification createNotification(String title, String body) {
        return Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
    }

    private AndroidConfig getAndroidConfig() {
        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setSound("default")
                        .build())
                .build();
    }

    private ApnsConfig getApnsConfig() {
        return ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setSound("default")
                        .build())
                .build();
    }
}
