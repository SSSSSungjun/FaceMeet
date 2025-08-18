package com.levelup.FaceMeet.security.intercepter;

import com.levelup.FaceMeet.security.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

// Spring Component로 등록하여 DI 받도록 변경 권장
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    // 생성자 주입
    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // 쿼리 파라미터에서 토큰 추출
        String uri = request.getURI().toString();
        System.out.println("웹 소켓 인증 과정에서 jwt uri"+ uri);

        String token = UriComponentsBuilder.fromUriString(uri).build().getQueryParams().getFirst("token");
        System.out.println("웹 소켓 인증 과정에서 jwt token"+ token);

        if (token != null && jwtUtil.verifyToken(token)) {
            Long userId = jwtUtil.getUserId(token);
            attributes.put("userId", userId);  // 나중에 Principal 대체 가능
            return true;
        }

        System.out.println("웹 소켓 인증 과정에서 jwt 토큰이 유효하지 않습니다");

        return false;
    }
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 필요시 구현
    }
}
