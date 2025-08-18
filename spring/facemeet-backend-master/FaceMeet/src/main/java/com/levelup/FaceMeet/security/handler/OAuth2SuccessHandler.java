package com.levelup.FaceMeet.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelup.FaceMeet.domain.User;
import com.levelup.FaceMeet.security.config.JwtProperties;
import com.levelup.FaceMeet.security.dto.jwt.GeneratedToken;
import com.levelup.FaceMeet.security.dto.response.AuthDTO.*;
import com.levelup.FaceMeet.security.util.JwtUtil;
import com.levelup.FaceMeet.security.util.RedisUtil;
import com.levelup.FaceMeet.service.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final UserService userService;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        String userAgent = request.getHeader("User-Agent");

        // OAuth2User로 캐스팅하여 인증된 사용자 정보를 가져온다.
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 소셜 로그인 제공자, 소셜 식별자를 가져온다.
        String provider = Optional.ofNullable(oAuth2User.getAttribute("provider"))
                .orElseThrow(() -> new IllegalArgumentException("Provider 정보 없음")).toString();

        String socialId = Optional.ofNullable(oAuth2User.getAttribute("socialId"))
                .orElseThrow(() -> new IllegalArgumentException("SocialId 정보 없음")).toString();

        // CustomOAuth2UserService에서 셋팅한 로그인한 회원 존재 여부를 가져온다.
        boolean isExist = oAuth2User.getAttribute("exist");

        // OAuth2User로부터 Role을 얻어온다.
        String role = oAuth2User.getAuthorities().stream()
                .findFirst()    // 첫번째 Role을 찾아온다.
                .orElseThrow(IllegalAccessError::new)   // 존재하지 않을 시 예외를 던진다.
                .getAuthority();

        // 회원이 존재하지 않을 경우 가입 처리
        if(!isExist) {
            userService.registerOAuthUser(socialId, provider, oAuth2User);
        }

        User user = userService.findBySocialIdAndProvider(socialId, provider);
        Long userId = user.getId();

        GeneratedToken token = jwtUtil.generateToken(userId, role);
        log.debug("jwtToken = {}", token.getAccessToken());

        redisUtil.save("refresh_token::" + userId, token.getRefreshToken(), Duration.ofMillis(refreshTokenExpirationMs));

        String clientType = (String) request.getSession().getAttribute("clientType");
        log.debug("clientType = " + clientType);

        if ("ROLE_ADMIN".equals(role)) {
            if("admin".equals(clientType)) handleAdminRedirect(response, token, user);
            else handleUserResponse(response, token, user);
        } else {
            if("admin".equals(clientType)) response.sendRedirect("https://i13d201.p.ssafy.io/app/");
            else handleUserResponse(response, token, user);
        }

        request.getSession().removeAttribute("clientType");
    }

    private void handleAdminRedirect(HttpServletResponse response, GeneratedToken token, User user) throws IOException {

        String baseUrl = "https://i13d201.p.ssafy.io/app/app/";
        
        String targetUrl = baseUrl + "?accessToken=" + URLEncoder.encode(token.getAccessToken(), StandardCharsets.UTF_8) +
                "&refreshToken=" + URLEncoder.encode(token.getRefreshToken(), StandardCharsets.UTF_8);

        response.sendRedirect(targetUrl);
    }

    private void handleUserResponse(HttpServletResponse response, GeneratedToken token, User user) throws IOException {
        // 응답 body에 토큰 넣기
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        AuthResponse tokenResponse = AuthResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .hasInfo(user.getNickname() != null)
                .hasFace(user.getFace() != null)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(tokenResponse);

        response.getWriter().write(json);
        response.getWriter().flush();
    }
}