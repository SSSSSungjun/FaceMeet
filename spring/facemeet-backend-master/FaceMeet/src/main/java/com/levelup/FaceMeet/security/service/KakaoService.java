package com.levelup.FaceMeet.security.service;

import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.security.dto.oauth2.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {

    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    @Value("${spring.security.oauth2.admin.kakao.admin-key}")
    private String adminKey;

    private final WebClient webClient;

    public KakaoService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://kapi.kakao.com")
                .build();
    }

    // 유저 연동 해제
    public void unlinkUser(String socialId) {
        try {
            String response = webClient.post()
                    .uri("/v1/user/unlink")
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                    .header("Authorization", "KakaoAK " + adminKey)
                    .bodyValue("target_id_type=user_id&target_id=" + socialId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("KAKAO unlink response: {}", response);
        } catch (Exception e) {
            log.error("KAKAO unlink error: {}", e.getMessage());
            throw new CustomException(ErrorCode.OAUTH2_UNLINK_FAILED, "카카오 연동 해제 중 오류가 발생 했습니다.");
        }
    }

    // 비동기 버전 연동 해제
    public Mono<String> unlinkUserAsync(String socialId) {
        return webClient.post()
                .uri("/v1/user/unlink")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .header("Authorization", "KakaoAK " + adminKey)
                .bodyValue("target_id_type=user_id&target_id=" + socialId)
                .retrieve()
                .bodyToMono(String.class);
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    KAKAO_USER_INFO_URL,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();

                Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
                String email = (String) kakaoAccount.get("email");

                KakaoUserInfo userInfo = new KakaoUserInfo();
                userInfo.setEmail(email);
//                userInfo.setId(body.get("id").toString());
                // nickname 등 필요한 필드도 파싱 가능

                return userInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
