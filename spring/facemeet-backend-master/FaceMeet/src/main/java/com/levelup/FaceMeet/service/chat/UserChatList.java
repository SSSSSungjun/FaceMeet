package com.levelup.FaceMeet.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelup.FaceMeet.domain.ChatRoom;
import com.levelup.FaceMeet.domain.ChatRoomMember;
import com.levelup.FaceMeet.domain.Message;
import com.levelup.FaceMeet.domain.User;
import com.levelup.FaceMeet.dto.ChatRoomDTO;
import com.levelup.FaceMeet.dto.MessageDTO;
import com.levelup.FaceMeet.repository.chat.ChatRoomMemberRepository;
import com.levelup.FaceMeet.repository.chat.ChatRoomRepository;
import com.levelup.FaceMeet.repository.chat.MessageRepository;
import com.levelup.FaceMeet.service.user.UserBlockService;
import com.levelup.FaceMeet.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserChatList {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserService userService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserBlockService userBlockService;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    // Redis 키 상수
    private static final String CHAT_LIST_KEY = "user_chat_list:";
    private static final String CHAT_DETAIL_KEY = "user_chat_detail:";
    private static final String CHAT_ORDER_KEY = "user_chat_order:";
    private static final String CHAT_LATEST_MESSAGE_KEY = "chat:room:";

    private static final Duration CACHE_TTL = Duration.ofDays(7);

    /**
     * 사용자별 채팅방 목록 조회
     */
    public List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse> chatListByUser(Long userId) {
        String key = CHAT_LIST_KEY + userId;

        try {
            // Redis에서 채팅방 ID 목록 조회
            String cachedData = redisTemplate.opsForValue().get(key);
            List<Long> chatRoomIds = null;

            if (cachedData != null) {

                chatRoomIds = objectMapper.readValue(cachedData, new TypeReference<List<Long>>() {});
            }

            // 캐시에 없다면 DB에서 조회 후 캐시 생성
            if (chatRoomIds == null || chatRoomIds.isEmpty()) {
                System.out.println("디비에서 찾음");
                return loadAndCacheChatList(userId);
            }

            // 각 채팅방의 상세 정보 조회
            List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse> resultList = new ArrayList<>();

            for (Long chatRoomId : chatRoomIds) {
                try {
                    ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse dto = getChatRoomDetail(userId, chatRoomId);

                    if (dto != null) {
                        // Redis에서 마지막 메시지 조회 및 설정
                        setLastMessageFromRedis(dto);
                        resultList.add(dto);
                    }
                } catch (Exception e) {
                    log.error("개별 채팅방 로딩 실패 - chatRoomId: {}, error: {}", chatRoomId, e.getMessage());
                }
            }

            // 빈 결과라면 DB에서 재조회
            if (resultList.isEmpty()) {
                return loadAndCacheChatList(userId);
            }

            // 최신 메시지 시간 기준으로 정렬
            sortByLastMessageTime(resultList);
            return resultList;

        } catch (Exception e) {
            log.error("Redis 오류, DB에서 직접 조회 - userId: {}, error: {}", userId, e.getMessage());
            return loadAndCacheChatList(userId);
        }
    }

    /**
     * DB에서 채팅방 목록을 조회하고 Redis에 캐시
     */
    private List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse> loadAndCacheChatList(Long userId) {
        try {
            List<ChatRoomMember> chatRoomMemberList = chatRoomMemberRepository.findByUserIdAndIsVisible(userId, true);
            log.debug("DB에서 조회된 채팅방 개수: {}", chatRoomMemberList.size());

            List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse> resultList = new ArrayList<>();
            List<Long> chatRoomIds = new ArrayList<>();

            for (ChatRoomMember member : chatRoomMemberList) {
                try {
                    ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse dto = buildChatRoomDTO(userId, member);

                    if (dto != null) {
                        resultList.add(dto);
                        chatRoomIds.add(member.getRoom().getId());

                        // Redis에 개별 채팅방 상세 정보 저장
                        saveChatRoomDetail(userId, dto);
                    }
                } catch (Exception e) {
                    log.error("채팅방 DTO 생성 실패 - memberId: {}, error: {}", member.getId(), e.getMessage());
                }
            }

            // 채팅방 ID 목록을 Redis에 저장
            cacheChatRoomIds(userId, chatRoomIds);

            // 마지막 메시지 시간 기준으로 정렬
            sortByLastMessageTime(resultList);
            return resultList;

        } catch (Exception e) {
            log.error("채팅방 목록 로딩 및 캐싱 실패 - userId: {}, error: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 채팅방 DTO 생성
     */
    private ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse buildChatRoomDTO(Long userId, ChatRoomMember member) {
        ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse dto =
                new ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse();

        ChatRoom chatRoom = member.getRoom();

        // 상대방 정보 찾기
        User partner = findPartnerUser(chatRoom, userId);
        if (partner == null) {
            log.warn("상대방을 찾을 수 없음 - chatRoomId: {}, userId: {}", chatRoom.getId(), userId);
            return null;
        }

        // 상대방 정보 설정
        setPartnerInfo(dto, partner);

        // 채팅방 정보 설정
        setChatRoomInfo(dto, chatRoom);

        // 메시지 및 읽지 않은 메시지 수 설정
        setMessageInfo(dto, chatRoom.getId(), partner.getId());

        // 차단 및 삭제 여부 설정
        setBlockAndDeleteInfo(dto, userId, partner);

        return dto;
    }

    /**
     * 상대방 사용자 찾기
     */
    private User findPartnerUser(ChatRoom chatRoom, Long userId) {
        if (chatRoom.getUser1().getId().equals(userId)) {
            return chatRoom.getUser2();
        } else if (chatRoom.getUser2().getId().equals(userId)) {
            return chatRoom.getUser1();
        }
        return null;
    }

    /**
     * 상대방 정보 설정
     */
    private void setPartnerInfo(ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse dto, User partner) {
        dto.setUserId(partner.getId());
        dto.setNickName(partner.getNickname());
        dto.setLastActivatedTime(partner.getLastSeen());
        dto.setIsOnline(partner.getIsOnline());
        dto.setImgUrl(partner.getFace() == null ? null : partner.getFace().getImg());
    }

    /**
     * 채팅방 정보 설정
     */
    private void setChatRoomInfo(ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse dto, ChatRoom chatRoom) {
        dto.setChatRoomId(chatRoom.getId());
        dto.setChatRoomStringId(chatRoom.getRoomStringId());
    }

    /**
     * 메시지 정보 설정
     */
    private void setMessageInfo(ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse dto, Long roomId, Long partnerId) {
        MessageDTO.ChatSummaryResponse summary = getLastMessageAndUnreadCount(roomId, partnerId);

        if (summary == null) {
            dto.setLastMessage(null);
            dto.setLastSendMessageTime(null);
            dto.setNonReadCnt(0L);
        } else {
            dto.setLastMessage(summary.getLastMessage());
            dto.setLastSendMessageTime(summary.getLastMessageTime());
            dto.setNonReadCnt(summary.getTotalUnreadCountFromPartner());
        }
    }

    /**
     * 차단 및 삭제 정보 설정
     */
    private void setBlockAndDeleteInfo(ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse dto, Long userId, User partner) {
        dto.setBlocked(userBlockService.isBlockedByPartner(userId, partner.getId()));
        dto.setDeleted(partner.getIsDeleted());
    }

    /**
     * Redis에서 마지막 메시지 정보 설정
     */
    private void setLastMessageFromRedis(ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse dto) {
        try {
            String redisKey = CHAT_LATEST_MESSAGE_KEY + dto.getChatRoomId() + ":latest";
            Map<Object, Object> lastChatValue = redisTemplate.opsForHash().entries(redisKey);

            // Redis에 마지막 메시지가 있는 경우
            if (!lastChatValue.isEmpty()) {
                Object content = lastChatValue.get("content");
                Object timestamp = lastChatValue.get("timestamp");

                if (content != null) {
                    String messageContent = content.toString();
                    // JSON 문자열인 경우 파싱해서 실제 내용 추출
                    if (messageContent.startsWith("\"") && messageContent.endsWith("\"")) {
                        try {
                            messageContent = objectMapper.readValue(messageContent, String.class);
                        } catch (JsonProcessingException e) {
                            // JSON 파싱 실패 시 따옴표만 제거
                            messageContent = messageContent.substring(1, messageContent.length() - 1);
                        }
                    }
                    dto.setLastMessage(messageContent);
                }

                if (timestamp != null) {
                    try {
                        long timestampLong = Long.parseLong(timestamp.toString());
                        dto.setLastSendMessageTime(LocalDateTime.ofEpochSecond(
                                timestampLong / 1000, 0, ZoneOffset.UTC));
                    } catch (NumberFormatException e) {
                        log.warn("타임스탬프 파싱 실패 - roomId: {}, timestamp: {}", dto.getChatRoomId(), timestamp);
                    }
                }
            } else {
                // Redis에 마지막 메시지가 없는 경우 DB에서 조회
                setLastMessageFromDB(dto);
            }
        } catch (Exception e) {
            log.error("Redis에서 마지막 메시지 조회 실패, DB에서 조회 시도 - roomId: {}, error: {}", dto.getChatRoomId(), e.getMessage());
            // Redis 오류 시에도 DB에서 조회 시도
            setLastMessageFromDB(dto);
        }
    }

    /**
     * DB에서 마지막 메시지 정보 설정 및 Redis에 저장
     */
    private void setLastMessageFromDB(ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse dto) {
        try {
            Optional<Message> lastMessageOpt = messageRepository.findTopByRoom_IdOrderBySendAtDesc(dto.getChatRoomId());

            if (lastMessageOpt.isPresent()) {
                Message lastMessage = lastMessageOpt.get();

                // DTO에 마지막 메시지 정보 설정
                dto.setLastMessage(lastMessage.getContent());
                dto.setLastSendMessageTime(lastMessage.getSendAt());

                // Redis에 마지막 메시지 정보 저장 (캐시 갱신)
                updateLastMessageInRedis(dto.getChatRoomId(), lastMessage.getContent(),
                        lastMessage.getSender().getId(), getReceiverId(lastMessage));

                log.debug("DB에서 마지막 메시지 조회 후 Redis 캐시 갱신 - roomId: {}", dto.getChatRoomId());
            } else {
                // 메시지가 없는 경우
                dto.setLastMessage(null);
                dto.setLastSendMessageTime(null);
            }
        } catch (Exception e) {
            log.error("DB에서 마지막 메시지 조회 실패 - roomId: {}, error: {}", dto.getChatRoomId(), e.getMessage());
            dto.setLastMessage(null);
            dto.setLastSendMessageTime(null);
        }
    }

    /**
     * 메시지의 수신자 ID 조회
     */
    private Long getReceiverId(Message message) {
        try {
            ChatRoom chatRoom = message.getRoom();
            Long senderId = message.getSender().getId();

            // 발신자가 아닌 사용자가 수신자
            if (chatRoom.getUser1().getId().equals(senderId)) {
                return chatRoom.getUser2().getId();
            } else {
                return chatRoom.getUser1().getId();
            }
        } catch (Exception e) {
            log.error("수신자 ID 조회 실패 - messageId: {}, error: {}", message.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * Redis에 마지막 메시지 정보 저장
     */
    private void updateLastMessageInRedis(Long roomId, String content, Long senderId, Long receiverId) {
        try {
            String redisKey = CHAT_LATEST_MESSAGE_KEY + roomId + ":latest";

            Map<String, Object> messageInfo = new HashMap<>();
            messageInfo.put("content", content);  // 문자열 직접 저장 (JSON 변환 안함)
            messageInfo.put("senderId", senderId);
            messageInfo.put("receiverId", receiverId);
            messageInfo.put("timestamp", System.currentTimeMillis());

            // Redis에 Hash로 저장
            redisTemplate.opsForHash().putAll(redisKey, messageInfo);

            // TTL 설정 (7일 후 만료)
            redisTemplate.expire(redisKey, Duration.ofDays(7));

            log.debug("Redis에 채팅방 {} 최신 메시지 저장 완료", roomId);

        } catch (Exception e) {
            log.error("Redis 메시지 저장 실패 - roomId: {}, error: {}", roomId, e.getMessage());
        }
    }

    /**
     * Redis에서 채팅방 상세 정보 조회
     */
    private ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse getChatRoomDetail(Long userId, Long chatRoomId) {
        try {
            String detailKey = CHAT_DETAIL_KEY + userId + ":" + chatRoomId;
            String jsonData = redisTemplate.opsForValue().get(detailKey);

            if (jsonData != null) {
                return objectMapper.readValue(jsonData,
                        ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse.class);
            }
        } catch (Exception e) {
            log.error("Redis에서 채팅방 상세 정보 조회 실패 - userId: {}, chatRoomId: {}, error: {}",
                    userId, chatRoomId, e.getMessage());
        }
        return null;
    }

    /**
     * Redis에 채팅방 상세 정보 저장
     */
    private void saveChatRoomDetail(Long userId, ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse dto) {
        try {
            String detailKey = CHAT_DETAIL_KEY + userId + ":" + dto.getChatRoomId();
            String jsonData = objectMapper.writeValueAsString(dto);
            redisTemplate.opsForValue().set(detailKey, jsonData, CACHE_TTL);
        } catch (Exception e) {
            log.error("Redis에 채팅방 상세 정보 저장 실패 - userId: {}, chatRoomId: {}, error: {}",
                    userId, dto.getChatRoomId(), e.getMessage());
        }
    }

    /**
     * 채팅방 ID 목록을 Redis에 캐시
     */
    private void cacheChatRoomIds(Long userId, List<Long> chatRoomIds) {
        try {
            String key = CHAT_LIST_KEY + userId;
            String json = objectMapper.writeValueAsString(chatRoomIds);
            redisTemplate.opsForValue().set(key, json, CACHE_TTL);
        } catch (Exception e) {
            log.error("채팅방 ID 목록 캐싱 실패 - userId: {}, error: {}", userId, e.getMessage());
        }
    }

    /**
     * 마지막 메시지 시간 기준으로 정렬
     */
    private void sortByLastMessageTime(List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse> resultList) {
        resultList.sort((dto1, dto2) -> {
            LocalDateTime time1 = dto1.getLastSendMessageTime();
            LocalDateTime time2 = dto2.getLastSendMessageTime();

            if (time1 == null && time2 == null) return 0;
            if (time1 == null) return 1;
            if (time2 == null) return -1;

            return time2.compareTo(time1);
        });
    }

    /**
     * 채팅방 순서 정보 업데이트
     */
    public void updateChatRoomOrder(Long userId, Long chatRoomId, LocalDateTime lastMessageTime) {
        try {
            String orderKey = CHAT_ORDER_KEY + userId;

            double score = lastMessageTime != null ?
                    lastMessageTime.toEpochSecond(ZoneOffset.UTC) : 0;

            redisTemplate.opsForZSet().add(orderKey, chatRoomId.toString(), score);
            redisTemplate.expire(orderKey, CACHE_TTL);
        } catch (Exception e) {
            log.error("채팅방 순서 업데이트 실패 - userId: {}, chatRoomId: {}, error: {}",
                    userId, chatRoomId, e.getMessage());
        }
    }

    /**
     * 마지막 메시지와 읽지 않은 메시지 수 조회
     */
    private MessageDTO.ChatSummaryResponse getLastMessageAndUnreadCount(Long roomId, Long partnerId) {
        try {
            // DB에 메시지가 하나도 없다면 null 반환
            long messageCountInDB = messageRepository.countByRoom_Id(roomId);
            if (messageCountInDB == 0) {
                return null;
            }

            // 읽지 않은 메시지 수 조회
            long unreadCountFromDB = messageRepository.countByRoom_IdAndSender_IdAndIsReadFalse(roomId, partnerId);
            log.debug("읽지 않은 메시지 수 - roomId: {}, partnerId: {}, count: {}",
                    roomId, partnerId, unreadCountFromDB);

            // 마지막 메시지 조회
            Optional<Message> messageOpt = messageRepository.findTopByRoom_IdOrderBySendAtDesc(roomId);
            if (messageOpt.isEmpty()) {
                log.warn("메시지가 존재해야 하는데 조회되지 않음 - roomId: {}", roomId);
                return null;
            }

            Message message = messageOpt.get();
            return new MessageDTO.ChatSummaryResponse(
                    message.getContent(),
                    message.getSendAt(),
                    unreadCountFromDB
            );
        } catch (Exception e) {
            log.error("마지막 메시지 및 읽지 않은 메시지 수 조회 실패 - roomId: {}, partnerId: {}, error: {}",
                    roomId, partnerId, e.getMessage());
            return null;
        }
    }

    /**
     * 사용자의 채팅방 캐시 무효화
     */
    public void invalidateUserChatCache(Long userId) {
        try {
            String listKey = CHAT_LIST_KEY + userId;
            String orderKey = CHAT_ORDER_KEY + userId;

            redisTemplate.delete(listKey);
            redisTemplate.delete(orderKey);

            // 상세 정보 캐시도 삭제 (패턴 매칭)
            String detailPattern = CHAT_DETAIL_KEY + userId + ":*";
            Set<String> detailKeys = redisTemplate.keys(detailPattern);
            if (detailKeys != null && !detailKeys.isEmpty()) {
                redisTemplate.delete(detailKeys);
            }
        } catch (Exception e) {
            log.error("사용자 채팅방 캐시 무효화 실패 - userId: {}, error: {}", userId, e.getMessage());
        }
    }
}