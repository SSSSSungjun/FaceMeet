package com.levelup.FaceMeet.config;

import com.levelup.FaceMeet.security.intercepter.JwtHandshakeInterceptor;
import com.levelup.FaceMeet.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableWebSocket //웹 소켓 서버 사용
@EnableWebSocketMessageBroker  //STOMP 사용
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Heartbeat 처리를 위한 TaskScheduler Bean 등록
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry registry) {
        //클라이언트가 특정 주제를 구독함
        registry.enableSimpleBroker("/sub")
                // Heartbeat 설정 추가
                .setHeartbeatValue(new long[]{10000, 10000}) // [서버→클라이언트, 클라이언트→서버] 10초
                .setTaskScheduler(taskScheduler()); // TaskScheduler 설정

        //클라이언트가 서버로 메시지 보낼 때 사용
        registry.setApplicationDestinationPrefixes("/pub");
        registry.setUserDestinationPrefix("/user");

        System.out.println("💓 Heartbeat 설정 완료: 서버↔클라이언트 10초 간격");
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/api/v1/ws")
//                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil))
                .setAllowedOriginPatterns("*")
                .withSockJS()
                // SockJS용 heartbeat 설정
                .setHeartbeatTime(25000); // 25초 간격 heartbeat

        //안드 전용 - Native WebSocket (STOMP heartbeat 10초 적용됨)
        registry.addEndpoint("/api/v1/websocket")
//                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil))
                .setAllowedOriginPatterns("*")
                .setAllowedOrigins("*")
                // 안드로이드용 추가 설정 (선택적)
                .setHandshakeHandler(new DefaultHandshakeHandler());

        System.out.println("🔧 WebSocket 엔드포인트 등록 완료:");
        System.out.println("   - /api/v1/ws (SockJS + JWT + Heartbeat 25s)");
        System.out.println("   - /api/v1/websocket (Native + JWT + STOMP Heartbeat 10s)");
    }
}