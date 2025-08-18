package com.levelup.FaceMeet.listener;

import com.levelup.FaceMeet.service.chat.SessionUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;


@Component
public class WebSocketEventListener {

    @Autowired
    private SessionUserService sessionUserService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String stompSessionId = headerAccessor.getSessionId();

        System.out.println("--- WebSocketEventListener: SessionConnectedEvent ---");
        System.out.println("새로운 WebSocket 연결 수립. STOMP 세션 ID: " + stompSessionId);
        System.out.println("-------------------------------------------------");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String stompSessionId = headerAccessor.getSessionId(); // 세션 ID로 제거
        sessionUserService.removeSessionByStompSessionId(stompSessionId);
        System.out.println("웹 소켓 연결이 종료되었습니다. 세션 ID: " + stompSessionId);
    }
}