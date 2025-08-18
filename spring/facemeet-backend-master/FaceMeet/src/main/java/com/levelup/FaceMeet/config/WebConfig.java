package com.levelup.FaceMeet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")  // 모든 API 경로에 대해 CORS 허용
//                        .allowedOriginPatterns("*") // 모든 출처(Origin) 허용
//                        .allowedMethods("*")        // 모든 HTTP 메소드 (GET, POST, PUT, DELETE, OPTIONS 등) 허용
//                        .allowedHeaders("*")        // 모든 요청 헤더 허용
//                        .allowCredentials(false);   // 인증 정보 (쿠키 등)는 허용하지 않음
//                // allowCredentials(true)와 allowedOriginPatterns("*")는 같이 쓸 수 없기 때문
//            }
//        };
//}
    
}