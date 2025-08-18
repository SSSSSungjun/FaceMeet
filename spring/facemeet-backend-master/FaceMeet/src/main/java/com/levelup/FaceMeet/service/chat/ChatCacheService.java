package com.levelup.FaceMeet.service.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelup.FaceMeet.domain.Message;
import com.levelup.FaceMeet.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

//redis 기반 채팅 캐시를 관리하는 서비스
@Service
public class ChatCacheService {

    @Autowired
    private  RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private  StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    //redis 키 패턴들
    private static final String ROOM_MESSAGES_KEY = "direct:chat:%s:messages"; // roomId
    private static final String USER_CHAT_LIST_KEY = "direct:user:%d:chats"; // userId
    private static final String USER_ONLINE_KEY = "direct:user:%d:online"; // userId
    private static final String UNREAD_COUNT_KEY = "direct:user:%d:unread:%s"; // userId, roomId
    private static final String TYPING_KEY = "direct:room:%s:typing:%d"; // roomId, userId
    private static final String LAST_MESSAGE_KEY = "direct:room:%s:last"; // roomId
    private static final String MESSAGE_READ_KEY = "direct:message:%d:read"; // messageId


    //최근 메시지 캐싱(Sorted Set 사용 -> 시간순 정렬)
    public void cacheMessage(MessageDTO.MessageSaveRequest req){

        String key = String.format(ROOM_MESSAGES_KEY, req.getRoomId());

        MessageDTO.MessageSendResponse response = new MessageDTO.MessageSendResponse(req.getContent() , req.getSenderId(), req.getReceiverId(), req.getRoomId(),LocalDateTime.now(), false, null );
        double score = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        redisTemplate.opsForZSet().add(key, response,score);

        //최근 100개 메시지만 유지 , 오래된 메시지 제거
        redisTemplate.opsForZSet().removeRange(key,0,-201);

        //24시간 TTL 설정
        redisTemplate.expire(key, Duration.ofDays(7));

        // 마지막 메시지 정보 업데이트
        cacheLastMessage(req);

        System.out.println("메시지 캐시 저장: "+ req.getRoomId()+" - "+ req.getContent());
    }

    //읽은 후 캐싱 시에 메시지 캐싱 하기
    public void afterReadCacheMessage(Message message){

        String key = String.format(ROOM_MESSAGES_KEY,message.getRoom().getRoomStringId());

        MessageDTO.MessageSendResponse response = new MessageDTO.MessageSendResponse(message.getContent() , message.getSender().getId(), message.getReceiver().getId(), message.getRoom().getId(), LocalDateTime.now(), message.getIsRead(), message.getReadAt() );
        double score = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        redisTemplate.opsForZSet().add(key, response,score);

        //최근 100개 메시지만 유지 , 오래된 메시지 제거
        redisTemplate.opsForZSet().removeRange(key,0,-201);

        //24시간 TTL 설정
        redisTemplate.expire(key, Duration.ofDays(7));

        MessageDTO.MessageSaveRequest req = new MessageDTO.MessageSaveRequest();
        req.setRoomId( message.getRoom().getId());
        req.setSenderId(message.getSender().getId());
        req.setReceiverId(message.getReceiver().getId());
        req.setContent(message.getContent());
        // 마지막 메시지 정보 업데이트
        cacheLastMessage(req);

        System.out.println("메시지 캐시 저장: "+ req.getRoomId()+" - "+ req.getContent());
    }

    //마지막 메시지 캐싱(채팅방 목록에서 사용)
    private void cacheLastMessage(MessageDTO.MessageSaveRequest req){

        String key = String.format(LAST_MESSAGE_KEY, req.getRoomId());

        Map<String, Object> lastMessageInfo = new HashMap<>();
        lastMessageInfo.put("content" , req.getContent());
        lastMessageInfo.put("senderId" , req.getSenderId());
        lastMessageInfo.put("createdAt" , LocalDateTime.now().toString());
        lastMessageInfo.put("type" , Message.MessageType.TEXT);

        redisTemplate.opsForValue().set(key, lastMessageInfo , Duration.ofDays(7));
    }

    //채팅방 최근 메시지 조회
    public List<MessageDTO.MessageSendResponse> getRecentMessages(String roomId, int limit) {
        String key = String.format(ROOM_MESSAGES_KEY, roomId);

        Set<Object> messages = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);

        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        // 이미 등록된 ObjectMapper 사용
        List<MessageDTO.MessageSendResponse> result = messages.stream()
                .map(obj -> {
                    try {
                        // Redis에서 가져온 객체를 MessageSendResponse로 변환
                        return objectMapper.convertValue(obj, MessageDTO.MessageSendResponse.class);
                    } catch (Exception e) {
                        // 변환 실패 시 로깅하고 예외 처리
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)  // null인 값은 필터링
                .collect(Collectors.toList());

        System.out.println("캐시에서 1대1 메시지 조회: " + roomId + " - " + result.size() + "개");
        return result;
    }

    //사용자 온라인 상태 관리
    public void setUserOnline(Long userId){
        String key = String.format(USER_ONLINE_KEY,userId);

        Map<String, Object> onlineInfo = new HashMap<>();
        onlineInfo.put("lastSeen" , LocalDateTime.now().toString());
        onlineInfo.put("isOnline" , true);

        redisTemplate.opsForValue().set(key, onlineInfo, Duration.ofMinutes(30));

        System.out.println("사용자 온라인 상태 설정: " +userId);
    }

    //사용자 오프라인 상태 관리
    public void setUserOffline(Long userId){
        String key = String.format(USER_ONLINE_KEY, userId);

        Map<String, Object> offlineInfo = new HashMap<>();
        offlineInfo.put("lastSeen", LocalDateTime.now().toString());
        offlineInfo.put("isOnline", false);

        redisTemplate.opsForValue().set(key, offlineInfo, Duration.ofHours(24));

        System.out.println("사용자 오프라인 상태 설정: " + userId);
    }

    //현재 사용자의 상태 가져오기
    public Map<String, Object> getUserOnlineStatus(Long userId) {
        String key = String.format(USER_ONLINE_KEY, userId);
        Object status = redisTemplate.opsForValue().get(key);

        if (status == null) {
            Map<String, Object> defaultStatus = new HashMap<>();
            defaultStatus.put("isOnline", false);
            defaultStatus.put("lastSeen", null);
            return defaultStatus;
        }

        return (Map<String, Object>) status;
    }

    // 읽지 않은 메시지 수 관리
    public void incrementUnreadCount(Long userId, String roomId) {
        String key = String.format(UNREAD_COUNT_KEY, userId, roomId);
        stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, Duration.ofDays(30)); // 30일 TTL

        System.out.println("읽지 않은 메시지 수 증가: " + userId + " - " + roomId);
    }

    public void resetUnreadCount(Long userId, String roomId) {
        String key = String.format(UNREAD_COUNT_KEY, userId, roomId);
        stringRedisTemplate.delete(key);

        System.out.println("읽지 않은 메시지 수 초기화: " + userId + " - " + roomId);
    }

    public int getUnreadCount(Long userId, String roomId) {
        String key = String.format(UNREAD_COUNT_KEY, userId, roomId);
        String count = stringRedisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    // 사용자별 전체 읽지 않은 메시지 수 조회
    public Map<String, Integer> getAllUnreadCounts(Long userId) {
        String pattern = String.format("direct:user:%d:unread:*", userId);
        Set<String> keys = stringRedisTemplate.keys(pattern);

        Map<String, Integer> unreadCounts = new HashMap<>();

        if (keys != null) {
            for (String key : keys) {
                String roomId = key.substring(key.lastIndexOf(':') + 1);
                String count = stringRedisTemplate.opsForValue().get(key);
                unreadCounts.put(roomId, count != null ? Integer.parseInt(count) : 0);
            }
        }

        return unreadCounts;
    }

    // 메시지 읽음 처리
    public void markMessageAsRead(Long messageId, Long readerId) {
        String key = String.format(MESSAGE_READ_KEY, messageId);

        Map<String, Object> readInfo = new HashMap<>();
        readInfo.put("readerId", readerId);
        readInfo.put("readAt", LocalDateTime.now().toString());

        redisTemplate.opsForValue().set(key, readInfo, Duration.ofDays(7));

        System.out.println("메시지 읽음 처리: " + messageId + " by " + readerId);
    }

    public Map<String, Object> getMessageReadInfo(Long messageId) {
        String key = String.format(MESSAGE_READ_KEY, messageId);
        Object readInfo = redisTemplate.opsForValue().get(key);
        return readInfo != null ? (Map<String, Object>) readInfo : null;
    }

    // 사용자의 채팅방 목록 캐싱
    public void addUserChatRoom(Long userId, String roomId, Long otherUserId) {
        String key = String.format(USER_CHAT_LIST_KEY, userId);

        Map<String, Object> chatRoomInfo = new HashMap<>();
        chatRoomInfo.put("roomId", roomId);
        chatRoomInfo.put("otherUserId", otherUserId);
        chatRoomInfo.put("lastActivity", LocalDateTime.now().toString());

        // Sorted Set으로 관리 (최근 활동 시간 기준)
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(key, chatRoomInfo, score);

        // 최근 50개 채팅방만 유지
        redisTemplate.opsForZSet().removeRange(key, 0, -51);

        redisTemplate.expire(key, Duration.ofDays(30));
    }

    public List<Map<String, Object>> getUserChatRooms(Long userId) {
        String key = String.format(USER_CHAT_LIST_KEY, userId);

        Set<Object> chatRooms = redisTemplate.opsForZSet().reverseRange(key, 0, -1);

        if (chatRooms == null) {
            return new ArrayList<>();
        }

        return chatRooms.stream()
                .map(obj -> (Map<String, Object>) obj)
                .collect(Collectors.toList());
    }

    // 마지막 메시지 정보 조회
    public Map<String, Object> getLastMessage(String roomId) {
        String key = String.format(LAST_MESSAGE_KEY, roomId);
        Object lastMessage = redisTemplate.opsForValue().get(key);
        return lastMessage != null ? (Map<String, Object>) lastMessage : null;
    }
}
