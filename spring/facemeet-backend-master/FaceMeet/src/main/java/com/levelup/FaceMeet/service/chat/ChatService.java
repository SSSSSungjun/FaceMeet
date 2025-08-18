package com.levelup.FaceMeet.service.chat;

import com.levelup.FaceMeet.domain.*;
import com.levelup.FaceMeet.dto.MatchDTO;
import com.levelup.FaceMeet.dto.MessageDTO;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.chat.ChatRoomMemberRepository;
import com.levelup.FaceMeet.repository.chat.ChatRoomRepository;
import com.levelup.FaceMeet.repository.chat.MessageRepository;
import com.levelup.FaceMeet.repository.match.MatchingRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import com.levelup.FaceMeet.service.user.UserBlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class ChatService {

    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    private static final String CHAT_LIST_KEY = "user_chat_list:";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String,MessageDTO.MessageSendResponse> messageSendResponseRedisTemplate;


    @Autowired
    private ChatCacheService chatCacheService;

    @Autowired
    private MatchingRepository matchingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserBlockService userBlockService;


    //1대 1 채팅방 생성 또는 조회
    public ChatRoom getOrCreateChatRoom(Long user1Id, Long user2Id){
        String roomId = generateChatRoomId(user1Id.toString(), user2Id.toString());

        ChatRoom existingRoom = chatRoomRepository.findByRoomStringId(roomId);
        User user1 = userRepository.findById(user1Id).orElseThrow( () -> new CustomException(ErrorCode.USER_NOT_FOUND, user1Id + " 사용자가 존재하지 않습니다"));
        User user2 = userRepository.findById(user2Id).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, user2Id + " 사용자가 존재하지 않습니다"));

        if(existingRoom != null){
            return existingRoom;
        }

        ChatRoom newRoom = new ChatRoom();
        newRoom.setUser1(user1);
        newRoom.setUser2(user2);
        newRoom.setCreatedAt(LocalDateTime.now());
        newRoom.setRoomStringId(roomId);

        ChatRoom savedRoom = chatRoomRepository.save(newRoom);
        ChatRoom chatRoom = chatRoomRepository.findByRoomStringId( roomId);


        //양쪽 사용자의 채팅방 목록에 추가
        ChatRoomMember chatRoomMember1 = new ChatRoomMember();
        chatRoomMember1.setUser(user1);
        chatRoomMember1.setIsVisible(true);
        chatRoomMember1.setRoom(chatRoom);

        ChatRoomMember chatRoomMember2 = new ChatRoomMember();
        chatRoomMember2.setUser(user2);
        chatRoomMember2.setIsVisible(true);
        chatRoomMember2.setRoom(chatRoom);


        chatRoomMemberRepository.save(chatRoomMember1);
        chatRoomMemberRepository.save(chatRoomMember2);


        chatCacheService.addUserChatRoom(user1Id,roomId, user2Id);
        chatCacheService.addUserChatRoom(user2Id,roomId,user1Id);

        System.out.println("새 1대 1 채팅방 : "+ roomId);
        return savedRoom;
    }
    //-----------------------------------------------------------------------//



    //회원별 채팅방 목록 조회
    //채팅방에 표시될 정보 ( 상대방 닉네임, 상대방의 현재활동중, 마지막 활동 시간, 마지막 내화내용, 마지막 대화 내용 읽음 여부 )

    //채팅방 id 생성
    public String generateChatRoomId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    //메시지 전송 , 마지막 메시지 redis에 저장
    public MessageDTO.MessageSendResponse sendMessage(MessageDTO.MessageSaveRequest req) {
        // 1. DB에 저장

        ChatRoom chatRoom = chatRoomRepository.findById(req.getRoomId())
                .orElseThrow( () -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        User sender = userRepository.findById(req.getSenderId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User receiver = userRepository.findById(req.getReceiverId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        //상대방에게 차단되었거나, 상대방이 탈퇴한 상태라면 현제 메시지를 보낼 수 없는 상태임을 알림
        if(userBlockService.isBlockedByPartner(sender,receiver) || receiver.getIsDeleted()){
            new CustomException(ErrorCode.CANT_CHAT_STATUS);
        }

        Message message = new Message();
        message.setRoom(chatRoom);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(req.getContent());
        message.setType(Message.MessageType.TEXT);
        message.setIsRead(false);

        messageRepository.save(message);


        MessageDTO.MessageSendResponse response = new MessageDTO.MessageSendResponse(req.getContent() , req.getSenderId(), req.getReceiverId(), req.getRoomId(), LocalDateTime.now(), false, null );

        //마지막 메시지 redis에서 업데이트
        updateLastMessageInRedis(req.getRoomId(), req.getContent(), req.getSenderId(), req.getReceiverId());

        invalidateUserChatCache(chatRoom.getUser1().getId());
        invalidateUserChatCache(chatRoom.getUser2().getId());


        System.out.println("메시지 전송 완료: " + req.getRoomId() + " - " + req.getContent());

        return response;
    }


    //redis에 마지막 메시지 저장

    private void updateLastMessageInRedis(Long roomId, String content, Long senderId, Long receiverId) {
        try {
            String redisKey = "chat:room:" + roomId + ":latest";

            // 메시지 정보 Map 생성
            Map<String, Object> messageInfo = new HashMap<>();
            messageInfo.put("content", content);
            messageInfo.put("senderId", senderId);
            messageInfo.put("receiverId", receiverId);
            // 현재 시간 사용
            long timestamp = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
            System.out.println("저장된 tiemstamp : " + timestamp);
            messageInfo.put("timestamp", timestamp);

            // Redis에 Hash로 저장
            redisTemplate.opsForHash().putAll(redisKey, messageInfo);

            // TTL 설정 (7일 후 만료)
            redisTemplate.expire(redisKey, Duration.ofDays(7));

            log.debug("Redis에 채팅방 {} 최신 메시지 저장 완료", roomId);

        } catch (Exception e) {
            log.error("Redis 메시지 저장 실패 - roomId: {}, error: {}", roomId, e.getMessage());
        }
    }
//메시지 읽음 처리
    public void markMessagesAsRead(Long readerId, Long roomId){

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow( () -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

//        //읽지않은 메시지 수 초기화
//        chatCacheService.resetUnreadCount(readerId, roomId);

        //db에서 읽지 않은 메시지 들을 읽음 처리
        List<Message> unreadMessages = messageRepository.findByRoom_IdAndReceiver_IdAndIsReadFalse(chatRoom.getId(), readerId);
        LocalDateTime now = LocalDateTime.now();
        unreadMessages.forEach(
                message -> {
                    message.setIsRead(true);
                    message.setReadAt(now);

                    chatCacheService.markMessageAsRead(message.getId(), readerId);
                }

        );
        if(!unreadMessages.isEmpty()){

            messageRepository.saveAll(unreadMessages);

            System.out.println("메시지 읽음 처리 : "+unreadMessages.size() +"개");
        }

        invalidateUserChatCache(chatRoom.getUser1().getId());
        invalidateUserChatCache(chatRoom.getUser2().getId());
    }

    private void invalidateUserChatCache(Long userId) {

        String listKey = CHAT_LIST_KEY + userId;


        redisTemplate.delete(listKey);


    }

    //해당 페이지의 내용들 조회
    public MessageDTO.AllMessageResponse getPageMessages(Long userId, int page, int limit, Long roomId) {
        MessageDTO.AllMessageResponse response = new MessageDTO.AllMessageResponse();
        // 4. 채팅방 정보 구성
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if(user.getRole() != User.Role.ADMIN){
            if(!chatRoom.getUser1().getId().equals(userId) && !chatRoom.getUser2().getId().equals(userId)){

                throw  new CustomException(ErrorCode.CANT_CHAT_OURS);
            }
        }



        // 2. 총 메시지 수 조회
        long totalMessages = messageRepository.countByRoom_Id(roomId);
        int totalPages = (int) Math.ceil((double) totalMessages / limit);

        // 3. 메시지 없으면 빈 응답 반환
        if (totalPages == 0) {
            response.setMessages(null);
        } else {
            // 0페이지 = 최신, 마지막페이지 = 가장 오래된
            Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "sendAt"));
            Page<Message> messagePage = messageRepository.findByRoom_Id(roomId, pageable);

            // 메시지 DTO로 변환
            List<MessageDTO.MessageSendResponse> messages = messagePage.getContent().stream().map(message -> {
                MessageDTO.MessageSendResponse dto = new MessageDTO.MessageSendResponse();
                dto.setContent(message.getContent());
                dto.setSenderId(message.getSender().getId());
                dto.setReceiverId(message.getReceiver().getId());
                dto.setRoomId(message.getRoom().getId());
                dto.setSendAt(message.getSendAt());
                dto.setIsRead(message.getIsRead());
                dto.setReadAt(message.getReadAt());
                return dto;
            }).collect(Collectors.toList());

             Collections.reverse(messages) ;

            // 메시지 페이징 정보 구성
            MessageDTO.PagedMessagesResponse pageMessageResponse = new MessageDTO.PagedMessagesResponse();
            pageMessageResponse.setMessages(messages);
            pageMessageResponse.setTotalPages(totalPages);
            pageMessageResponse.setCurrentPage(page);

            response.setMessages(pageMessageResponse);
        }


        User partner = null;
        if(user.getId().equals(chatRoom.getUser1().getId())) {
            partner = chatRoom.getUser2();
        }
        else{
            partner = chatRoom.getUser1();
        }

        System.out.println("user1Id : "+chatRoom.getUser1().getId() + " "+"user2Id: "+ chatRoom.getUser2().getId());
        System.out.println("user1id " + user.getId());
        System.out.println("user2Id "+ partner.getId());

        MatchDTO.MathchingSucessResponse matchDTO = new MatchDTO.MathchingSucessResponse();
        matchDTO.setChatRoomId(chatRoom.getId());
        matchDTO.setPartnerId(partner.getId());
        matchDTO.setPartnerNickname(partner.getNickname());
        Boolean isBlocked1 =  userBlockService.isBlockedByPartner(user.getId(),partner.getId());
        Boolean isBolocked2 = userBlockService.isBlockedByPartner(partner.getId() , user.getId());
        if(isBlocked1 || isBolocked2){
            matchDTO.setBlocked(true);
            System.out.println("blocked가 true임");
        }else{
            System.out.println("blocked가 false임");
            matchDTO.setBlocked(false);
        }

        matchDTO.setDeleted(partner.getIsDeleted());
        matchDTO.setImgUrl(partner.getFace() != null ? partner.getFace().getImg() : null);

        Matching matching = matchingRepository.findByRequesterAndAccepter(user, partner);
        if(matching == null) {
            matching = matchingRepository.findByRequesterAndAccepter(partner, user);
        }
        matchDTO.setSimilar(matching != null ? matching.getCompatibility().doubleValue() : null);

        response.setChatRoom(matchDTO);

        // 디버깅 로그
        System.out.println("messages : " + response.getMessages());
        System.out.println("chatRooms : " + response.getChatRoom());


        return response;
    }



    //채팅방의 메시지 조회 페이징 처리
    public MessageDTO.AllMessageResponse getLastPageMessages(Long userId, int limit, Long roomId) {
        MessageDTO.AllMessageResponse response = new MessageDTO.AllMessageResponse();

        // 4. 채팅방 정보 구성
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(user.getRole() != User.Role.ADMIN){
            if(!chatRoom.getUser1().getId().equals(userId) && !chatRoom.getUser2().getId().equals(userId)){

                throw  new CustomException(ErrorCode.CANT_CHAT_OURS);
            }
        }





        // 2. 총 메시지 수 조회
        long totalMessages = messageRepository.countByRoom_Id(roomId);
        int totalPages = (int) Math.ceil((double) totalMessages / limit);

        // 3. 메시지 없으면 빈 응답 반환
        if (totalPages == 0) {
            response.setMessages(null);
        } else {
            // 마지막 페이지 = 가장 오래된 메시지들
            int lastPage = totalPages - 1;

            Pageable pageable = PageRequest.of(lastPage, limit, Sort.by(Sort.Direction.DESC, "sendAt"));
            Page<Message> messagePage = messageRepository.findByRoom_Id(roomId, pageable);

            // 메시지 DTO로 변환
            List<MessageDTO.MessageSendResponse> messages = messagePage.getContent().stream().map(message -> {
                MessageDTO.MessageSendResponse dto = new MessageDTO.MessageSendResponse();
                dto.setContent(message.getContent());
                dto.setSenderId(message.getSender().getId());
                dto.setReceiverId(message.getReceiver().getId());
                dto.setRoomId(message.getRoom().getId());
                dto.setSendAt(message.getSendAt());
                dto.setIsRead(message.getIsRead());
                dto.setReadAt(message.getReadAt());
                return dto;
            }).collect(Collectors.toList());

            Collections.reverse(messages) ;

            // 메시지 페이징 정보 구성
            MessageDTO.PagedMessagesResponse pageMessageResponse = new MessageDTO.PagedMessagesResponse();
            pageMessageResponse.setMessages(messages);
            pageMessageResponse.setTotalPages(totalPages);
            pageMessageResponse.setCurrentPage(lastPage);

            response.setMessages(pageMessageResponse);
        }



        User partner = null;
        if(user.getId().equals(chatRoom.getUser1().getId())) {
            partner = chatRoom.getUser2();
        }
        else{
            partner = chatRoom.getUser1();
        }

        MatchDTO.MathchingSucessResponse matchDTO = new MatchDTO.MathchingSucessResponse();
        matchDTO.setChatRoomId(chatRoom.getId());
        matchDTO.setPartnerId(partner.getId());
        matchDTO.setPartnerNickname(partner.getNickname());
        System.out.println("partner : " + partner.getId());
        Boolean isBlocked1 =  userBlockService.isBlockedByPartner(user.getId(),partner.getId());
        Boolean isBolocked2 = userBlockService.isBlockedByPartner(partner.getId() , user.getId());
        if(isBlocked1 || isBolocked2){
            matchDTO.setBlocked(true);
        }else{
            matchDTO.setBlocked(false);
        }

        matchDTO.setDeleted(partner.getIsDeleted());
        matchDTO.setImgUrl(partner.getFace() != null ? partner.getFace().getImg() : null);

        Matching matching = matchingRepository.findByRequesterAndAccepter(user, partner);
        if(matching == null) {
            matching = matchingRepository.findByRequesterAndAccepter(partner, user);
        }
        matchDTO.setSimilar(matching != null ? matching.getCompatibility().doubleValue() : null);

        response.setChatRoom(matchDTO);

        // 디버깅 로그
        System.out.println("Last page messages : " + response.getMessages());
        System.out.println("chatRooms : " + response.getChatRoom());

        return response;
    }


    //특정 채팅방의 모든 메시지 조회
    public MessageDTO.AllMessageResponse getAllMessages(Long userId, Long roomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 메시지 전체 조회 (시간 순 정렬)
        List<Message> allMessages = messageRepository.findByRoom_IdOrderBySendAtAsc(roomId);

        // 2. DTO로 변환
        List<MessageDTO.MessageSendResponse> messages = new ArrayList<>();
        for (Message message : allMessages) {
            MessageDTO.MessageSendResponse messageSendResponse = new MessageDTO.MessageSendResponse();
            messageSendResponse.setContent(message.getContent());
            messageSendResponse.setSenderId(message.getSender().getId());
            messageSendResponse.setReceiverId(message.getReceiver().getId());
            messageSendResponse.setRoomId(message.getRoom().getId());
            messageSendResponse.setSendAt(message.getSendAt());
            messageSendResponse.setIsRead(message.getIsRead());
            messageSendResponse.setReadAt(message.getReadAt());
            messages.add(messageSendResponse);
        }

        // 3. 응답 생성 (totalPages는 의미 없지만 형식상 유지)
        MessageDTO.PagedMessagesResponse pageMessageResponse = new MessageDTO.PagedMessagesResponse();
        pageMessageResponse.setMessages(messages);
        pageMessageResponse.setTotalPages(1); // 또는 totalPages 필드 자체 제거 가능
        MessageDTO.AllMessageResponse response = new MessageDTO.AllMessageResponse();

        MatchDTO.MathchingSucessResponse matchDTOresponse = new MatchDTO.MathchingSucessResponse();
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow( () -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        matchDTOresponse.setChatRoomId(chatRoom.getId());
        User partner = chatRoom.getUser1().getId() == userId ? chatRoom.getUser2() : chatRoom.getUser1();
        matchDTOresponse.setPartnerId(partner.getId());
        matchDTOresponse.setPartnerNickname(partner.getNickname());
        matchDTOresponse.setImgUrl(partner.getFace() == null ? null : partner.getFace().getImg());

        Matching matching = matchingRepository.findByRequesterAndAccepter(user,partner);
        matchDTOresponse.setSimilar(matching == null ? null : matching.getCompatibility().doubleValue());
        response.setMessages(pageMessageResponse);
        response.setChatRoom(matchDTOresponse);

        System.out.println("messages : "+ response.getMessages());
        System.out.println("chatRooms : " + response.getChatRoom());
        return response;
    }



}
