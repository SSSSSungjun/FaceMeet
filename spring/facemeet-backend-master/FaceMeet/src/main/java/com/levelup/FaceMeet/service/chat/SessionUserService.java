package com.levelup.FaceMeet.service.chat;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionUserService {

    // key: userId (1, 2), value: stompSessionId (ns1e1vu4)
    private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();
    // key: stompSessionId, value: userId
    private final Map<String, String> sessionIdUserMap = new ConcurrentHashMap<>();

    public void addUserSession(String userId, String stompSessionId) {
        userSessionMap.put(userId, stompSessionId);
        sessionIdUserMap.put(stompSessionId, userId);
        System.out.println("사용자 세션 추가됨: User=" + userId + ", Session=" + stompSessionId +
                ". 현재 연결된 유저 수: " + userSessionMap.size());
    }

    public void removeSessionByStompSessionId(String stompSessionId) {
        String userId = sessionIdUserMap.remove(stompSessionId);
        if (userId != null) {
            userSessionMap.remove(userId);
            System.out.println("세션 제거됨: User=" + userId + ", Session=" + stompSessionId +
                    ". 현재 연결된 유저 수: " + userSessionMap.size());
        } else {
            System.out.println("세션 ID로 찾을 수 없는 연결 제거 요청: " + stompSessionId);
        }
    }

    public boolean isUserConnected(String userId) {
        System.out.println("isUserConnected 호출됨: " + userId + ". 맵에 존재하는가? " + userSessionMap.containsKey(userId));
        return userSessionMap.containsKey(userId);
    }

    public String getStompSessionId(String userId) {
        return userSessionMap.get(userId);
    }
}