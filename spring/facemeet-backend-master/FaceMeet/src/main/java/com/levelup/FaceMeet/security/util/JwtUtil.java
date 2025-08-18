package com.levelup.FaceMeet.security.util;

import com.levelup.FaceMeet.security.config.JwtProperties;
import com.levelup.FaceMeet.security.dto.jwt.GeneratedToken;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = jwtProperties.getSecret();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     *
     * @param userId
     * @param role
     * @return access token, refresh token이 담긴 generated token 반환
     */
    public GeneratedToken generateToken(Long userId, String role) {
        String refreshToken = generateRefreshToken(userId, role);
        String accessToken = generateAccessToken(userId, role);

        return new GeneratedToken(accessToken, refreshToken);
    }

    public String generateRefreshToken(Long userId, String role) {
        // 토큰의 유효 기간을 밀리초 단위로 설정
        long refreshPeriod = jwtProperties.getRefreshTokenExpirationMs();

        // 새로운 클레임 객체를 생성하고, userId와 권한 셋팅
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("role", role);

        // 현재 시간과 날짜를 가져온다.
        Date now = new Date();

        return Jwts.builder()
                // Payload를 구성하는 속성들을 정의한다.
                .setClaims(claims)
                // 발행일자를 넣는다.
                .setIssuedAt(now)
                // 토큰의 만료일시를 설정한다.
                .setExpiration(new Date(now.getTime() + refreshPeriod))
                // 지정된 서명 알고리즘과 비밀 키를 사용하여 토큰을 서명한다.
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(Long userId, String role) {

        long tokenPeriod = jwtProperties.getAccessTokenExpirationMs();

        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));

        claims.put("role", role);

        Date now = new Date();

        return
                Jwts.builder()
                        .setClaims(claims)
                        .setIssuedAt(now)
                        .setExpiration(new Date(now.getTime() + tokenPeriod))
                        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                        .compact();
    }

    /**
     * HTTP Authorization 헤더에서 전달된 토큰 문자열에서 "Bearer " 접두어를 제거하여 순수 JWT 토큰 문자열을 반환한다.
     *
     * @param token HTTP Authorization 헤더에서 전달된 토큰 문자열 (예: "Bearer {jwt}")
     * @return "Bearer "가 제거된 JWT 토큰 문자열, 만약 접두어가 없으면 원본 토큰 그대로 반환
     */
    private String cleanToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    private Claims extractClaims(String token) {
        String cleanedToken = cleanToken(token);
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(cleanedToken)
                .getBody();
    }

    private Claims extractClaimsIgnoreExpiration(String token) {
        try {
            return extractClaims(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public boolean verifyToken(String token) {
        try {
            String cleanedToken = cleanToken(token);
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // 새로운 방식
                    .build()
                    .parseClaimsJws(cleanedToken);  // 유효한 토큰이면 여기까지 통과

            return true;
        } catch (ExpiredJwtException e) {
            log.debug("토큰 만료");
            return false;
        } catch (JwtException e) {
            log.debug("토큰 검증 실패: " + e.getMessage());
            return false;
        }
    }

    // 토큰에서 userId(Subject) 추출
    public Long getUserId(String token) {
        String subject = extractClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    // 토큰에서 role(권한) 추출
    public String getRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * 토큰의 남은 만료시간을 밀리초 단위로 반환한다.
     *
     * @param token JWT 토큰 문자열
     * @return 남은 만료시간 (밀리초), 만료된 경우 0 반환
     */
    public long getRemainingExpirationTime(String token) {
        try {
            Claims claims = extractClaims(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            long remainingTime = expiration.getTime() - now.getTime();
            return Math.max(0, remainingTime); // 음수인 경우 0 반환
        } catch (ExpiredJwtException e) {
            log.debug("토큰이 만료됨");
            return 0;
        } catch (JwtException e) {
            log.debug("토큰 파싱 실패: " + e.getMessage());
            return 0;
        }
    }
}