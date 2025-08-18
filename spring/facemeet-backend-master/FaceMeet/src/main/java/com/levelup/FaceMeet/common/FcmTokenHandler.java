package com.levelup.FaceMeet.common;

import com.google.firebase.messaging.TopicManagementResponse;
import com.levelup.FaceMeet.repository.fcm.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class FcmTokenHandler {

    private final FcmTokenRepository fcmTokenRepository;

    public void handleFailedToken(String token, TopicManagementResponse.Error error) {
        // 우선 로깅만 하고 실제 구조 파악
        log.warn("토픽 작업 실패 토큰: {}, 에러: {}", maskToken(token), error);

        // 에러 내용을 확인한 후에 비활성화 로직 추가
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) return "***";
        return token.substring(0, 10) + "...";
    }
}
