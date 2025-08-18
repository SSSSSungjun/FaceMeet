package com.levelup.FaceMeet.security.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String email;
    private String provider;
    private String name;

    private String kakaoAccessToken;    // 클라이언트가 카카오에서 받은 액세스 토큰을 이 필드로 보냄

}