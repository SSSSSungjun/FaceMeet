package com.levelup.FaceMeet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // Security Scheme 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Security Requirement 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("Bearer Token");
//추가해볼까
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes("Bearer Token", securityScheme)) // 모든 API에 토큰을 적용
                .servers(List.of(
                    new io.swagger.v3.oas.models.servers.Server()
                        .url("https://i13d201.p.ssafy.io") // HTTPS 주소로 변경
                        .description("Production Server (HTTPS)"),
                    new io.swagger.v3.oas.models.servers.Server()
                            .url("http://localhost:8080")
                            .description("Localhost (HTTP)")
        ));
    }

    private Info apiInfo() {
        return new Info()
                .title("Sanggyeonrye 상견례 API")
                .description("<h3>Sanggyeonrye에서 사용되는 RESTful API에 대한 문서를 제공한다.</h3>")
                .version("1.0.0");

    }
}
