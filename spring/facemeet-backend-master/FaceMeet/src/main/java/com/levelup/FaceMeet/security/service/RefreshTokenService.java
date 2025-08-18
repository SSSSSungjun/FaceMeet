package com.levelup.FaceMeet.security.service;

import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.security.util.JwtUtil;
import com.levelup.FaceMeet.security.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token::";

    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private String getRefreshTokenKey(Long userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }

    public void saveRefreshToken(Long userId, String refreshToken) {
        try {
            String key = getRefreshTokenKey(userId);
            redisUtil.save(key, refreshToken, Duration.ofMillis(refreshTokenExpirationMs));
            log.debug("유저 {}의 refresh token이 저장되었습니다.", userId);
        } catch (Exception e) {
            log.error("Refresh token 저장 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public String getRefreshToken(Long userId) {
        try {
            String key = getRefreshTokenKey(userId);
            return redisUtil.get(key)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Refresh token 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void removeRefreshToken(Long userId) {
        try {
            String key = getRefreshTokenKey(userId);
            redisUtil.delete(key);
            log.debug("유저 {}의 refresh token이 삭제되었습니다.", userId);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void removeRefreshTokenByAccessToken(String accessToken) {
        try {
            // access token에서 userId 추출
            Long userId = jwtUtil.getUserId(accessToken);
            String key = getRefreshTokenKey(userId);
            redisUtil.delete(key);
            log.debug("Access token으로 유저 {}의 refresh token이 삭제되었습니다.", userId);
        } catch (Exception e) {
            log.error("Access token으로 refresh token 삭제 실패 - accessToken: {}, error: {}",
                    accessToken, e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
