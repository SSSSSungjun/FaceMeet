package com.levelup.FaceMeet.security.controller;

import com.levelup.FaceMeet.dto.UserInfoDTO;
import com.levelup.FaceMeet.security.config.JwtProperties;
import com.levelup.FaceMeet.security.dto.CustomUserDetails;
import com.levelup.FaceMeet.security.dto.response.AuthDTO;
import com.levelup.FaceMeet.security.dto.response.AuthDTO.*;
import com.levelup.FaceMeet.security.dto.response.StatusResponse;
import com.levelup.FaceMeet.security.dto.response.TokenResponseStatus;
import com.levelup.FaceMeet.security.service.RefreshTokenService;
import com.levelup.FaceMeet.security.util.JwtUtil;
import com.levelup.FaceMeet.security.util.RedisUtil;
import com.levelup.FaceMeet.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "AuthController", description = "회원가입/로그인/로그아웃 및 토큰 관리 기능 제공")
public class AuthController {

    private final RedisUtil redisUtil;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final JwtProperties jwtProperties;

    @GetMapping("/oauth2/kakao")
    @Operation(summary = "카카오 소셜 로그인 URL 제공 (Swagger 테스트용)", description = "카카오 소셜 로그인에 필요한 인증 URL을 반환합니다.")
    public ResponseEntity<?> getKakaoLoginUrl() {
        String redirectUri = "http://i13d201.p.ssafy.io/oauth2/authorization/kakao"; // 스프링 oauth2 리디렉션 경로
        return ResponseEntity.ok(Map.of("kakaoLoginUrl", redirectUri));
    }

    @GetMapping("/oauth2/naver")
    @Operation(summary = "네이버 소셜 로그인 URL 제공 (Swagger 테스트용)", description = "네이버 소셜 로그인에 필요한 인증 URL을 반환합니다.")
    public ResponseEntity<?> getNaverLoginUrl() {
        String redirectUri = "http://i13d201.p.ssafy.io/oauth2/authorization/naver"; // 스프링 oauth2 리디렉션 경로
        return ResponseEntity.ok(Map.of("naverLoginUrl", redirectUri));
    }

    @GetMapping("/oauth2/admin/kakao")
    @Operation(summary = "관리자 페이지 카카오 소셜 로그인", description = "관리자용 카카오 소셜 로그인 기능")
    public void adminKakaoLogin(HttpServletResponse response) throws IOException {
        String redirectUri = "https://i13d201.p.ssafy.io/oauth2/authorization/kakao?client_type=admin";
        response.sendRedirect(redirectUri);
    }

    @GetMapping("/oauth2/admin/naver")
    @Operation(summary = "관리자 페이지 네이버 소셜 로그인", description = "관리자용 네이버 소셜 로그인 기능")
    public void adminNaverLogin(HttpServletResponse response) throws IOException {
        String redirectUri = "https://i13d201.p.ssafy.io/oauth2/authorization/naver?client_type=admin";
        response.sendRedirect(redirectUri);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 처리", description = "로그아웃 기능입니다.")
    public ResponseEntity<StatusResponse> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {

        // refresh token 제거
        refreshTokenService.removeRefreshToken(userDetails.getUserId());

        // access token 처리 생각

        return ResponseEntity.ok(StatusResponse.addStatus(200));
    }

    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 재발급", description = "Response Body에 담긴 리프레시 토큰을 확인하여, 유효하면 새로운 액세스 토큰을 Response Body에 담아 재발급합니다.")
    public ResponseEntity<?> refresh(@RequestBody AuthDTO.RefreshRequest request) {
        try {

            String refreshToken = request.getRefreshToken();

            if (refreshToken == null) {
                return ResponseEntity
                        .status(400)
                        .body(TokenResponseStatus.addStatus(400, "refresh token이 cookie에 없습니다."));
            }

            // 토큰 유효성 검사 (만료 여부 포함)
            if (!jwtUtil.verifyToken(refreshToken)) {
                return ResponseEntity.status(401).body(TokenResponseStatus.addStatus(401, "만료된 토큰입니다."));
            }

            // refresh token에서 유저 식별자, 권한 추출
            Long userId = jwtUtil.getUserId(refreshToken);
            String role = jwtUtil.getRole(refreshToken);

            // Redis에서 저장된 refresh token 조회
            String storedRefreshToken = refreshTokenService.getRefreshToken(userId);
            // 클라이언트에서 온 토큰과 Redis에 저장된 토큰 비교
            if (!refreshToken.equals(storedRefreshToken)) {
                return ResponseEntity.status(401).body(TokenResponseStatus.addStatus(401, "유효하지 않은 토큰입니다."));
            }

            // 새로운 access token 발급
            String newAccessToken = jwtUtil.generateAccessToken(userId, role);

            long remainTimeMs = jwtUtil.getRemainingExpirationTime(refreshToken);
            long refreshTokenTotalValidity = jwtProperties.getRefreshTokenExpirationMs();
            double remainRatio = remainTimeMs / (double) refreshTokenTotalValidity;

            if (remainRatio <= 0.2) {
                String newRefreshToken = jwtUtil.generateRefreshToken(userId, role);

                // redis refresh token 교체
                refreshTokenService.saveRefreshToken(userId, newRefreshToken);
                return ResponseEntity.ok(RefreshResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).build());
            } else {
                return ResponseEntity.ok(RefreshResponse.builder().accessToken(newAccessToken).refreshToken(refreshToken).build());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500).body(TokenResponseStatus.addStatus(500, "INTERNAL SERVER ERROR."));
        }
    }

    @PostMapping("/onboarding")
    @Operation(summary = "회원 가입 추가 정보 처리", description = "회원가입 이후 추가 정보를 받아 신규 사용자의 초기 정보를 업데이트 합니다.")
    public ResponseEntity<StatusResponse> signup(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UserInfoDTO.UserInfoUpdateRequest signupRequest) {

        Long userId = userDetails.getUserId();

        userService.updateUser(userId, signupRequest);

        return ResponseEntity.ok(StatusResponse.addStatus(200));
    }
}
