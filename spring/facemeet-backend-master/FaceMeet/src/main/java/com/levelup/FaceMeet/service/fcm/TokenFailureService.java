package com.levelup.FaceMeet.service.fcm;

import com.google.firebase.messaging.*;
import com.levelup.FaceMeet.repository.fcm.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * FCM 토큰 실패 처리 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenFailureService {

    private final FcmTokenRepository fcmTokenRepository;

    /**
     * 배치 응답에서 실패한 토큰들 처리
     */
    @Transactional
    public void handleFailedTokens(List<String> tokens, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);

            if (!sendResponse.isSuccessful()) {
                String failedToken = tokens.get(i);
                FirebaseMessagingException exception = sendResponse.getException();

                handleFailedToken(failedToken, exception);
            }
        }
    }

    /**
     * 개별 실패 토큰 처리
     */
    private void handleFailedToken(String token, FirebaseMessagingException exception) {
        MessagingErrorCode errorCode = exception.getMessagingErrorCode();

        log.warn("토큰 전송 실패: token={}, errorCode={}, message={}",
                maskToken(token), errorCode, exception.getMessage());

        if (shouldDeactivateToken(errorCode)) {
            deactivateToken(token);
        }
    }

    /**
     * 토큰 비활성화 여부 판단
     */
    private boolean shouldDeactivateToken(MessagingErrorCode errorCode) {
//        return errorCode == MessagingErrorCode.UNREGISTERED ||
        return errorCode == MessagingErrorCode.INVALID_ARGUMENT ||
                errorCode == MessagingErrorCode.SENDER_ID_MISMATCH;
    }

    /**
     * 토큰 비활성화
     */
    private void deactivateToken(String token) {
        fcmTokenRepository.findByDeviceToken(token)
                .ifPresent(fcmToken -> {
                    fcmToken.setIsActive(false);
                    fcmTokenRepository.save(fcmToken);
                    log.info("토큰 비활성화 완료: {}", maskToken(token));
                });
    }

    /**
     * 토큰 마스킹 (로그용)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 6) + "***";
    }
}