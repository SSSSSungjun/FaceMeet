package com.levelup.FaceMeet.service.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelup.FaceMeet.domain.*;
import com.levelup.FaceMeet.dto.ChatRoomDTO;
import com.levelup.FaceMeet.dto.MatchDTO;
import com.levelup.FaceMeet.dto.MessageDTO;
import com.levelup.FaceMeet.exception.CustomException;
import com.levelup.FaceMeet.exception.ErrorCode;
import com.levelup.FaceMeet.repository.chat.ChatRoomMemberRepository;
import com.levelup.FaceMeet.repository.chat.ChatRoomRepository;
import com.levelup.FaceMeet.repository.chat.MessageRepository;
import com.levelup.FaceMeet.repository.match.MatchingRepository;
import com.levelup.FaceMeet.repository.user.UserBlockRepository;
import com.levelup.FaceMeet.repository.user.UserRepository;
import com.levelup.FaceMeet.service.user.UserBlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MatchingRepository matchingRepository;

//    @Autowired
//    private RedisTemplate<String, MessageDTO.MessageSendResponse> redisTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserBlockService userBlockService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserBlockRepository userBlockRepository;
    ;
    // Redis 키 상수
    private static final String CHAT_LIST_KEY = "user_chat_list:";
    //회원별 채팅방 목록

    public List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse> chatListByUser(Long userId){
        System.out.println("=====================유저별 채팅방 목록 불러오기 ==============================");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //회원별 나가지 않은 채팅방
        List< ChatRoomMember> chatRoomMemberList = chatRoomMemberRepository.findByUserAndIsVisible(user , true);

        System.out.println("chatRoomMemberList : " + chatRoomMemberList);

        List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse> resultList = new ArrayList<>();
        for(ChatRoomMember member  : chatRoomMemberList){

            ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse dto = new ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse();

            //상대방이 누구인지 찾기
            ChatRoom chatRoom = member.getRoom();
            User partner = chatRoom.getUser1().getId() == userId ? chatRoom.getUser2() : chatRoom.getUser1();

            //보여줘야 할 상대방 정보 저장
            dto.setUserId(partner.getId());
            dto.setNickName(partner.getNickname());
            dto.setLastActivatedTime(partner.getLastSeen());
            dto.setIsOnline(partner.getIsOnline());
            dto.setImgUrl(partner.getFace() == null ? null : partner.getFace().getImg());


            //보여줄 채팅방 정보 설정
            dto.setChatRoomId(chatRoom.getId());
            dto.setChatRoomStringId(chatRoom.getRoomStringId());
            MessageDTO.ChatSummaryResponse summary = getLastMessageAndUnreadCount(chatRoom.getId(), partner.getId());
            System.out.println("summary : " + summary);
            if(summary == null){
                dto.setLastMessage(null);
                dto.setNonReadCnt(0L);
            } else {
                // summary가 null이 아닐 때만 접근
                dto.setLastMessage(summary.getLastMessage() != null ? summary.getLastMessage().toString() : null);
                dto.setLastSendMessageTime(summary.getLastMessageTime());
                dto.setNonReadCnt(summary.getTotalUnreadCountFromPartner());
            }
            System.out.println("summary : " + summary);

            //내가 차단을 했는지  +  차단을 당했는디
            dto.setBlocked(userBlockService.isBlockedByPartner(user, partner));
            System.out.println("partner : " + partner.getId() + " partner_deleted : " + partner.getIsDeleted());
            dto.setDeleted(partner.getIsDeleted());
            resultList.add(dto);


        }

        //마지막으로 보낸 메시지 시간 기준으로 정렬
        resultList.sort((dto1, dto2) -> {
            LocalDateTime time1 = dto1.getLastSendMessageTime();
            LocalDateTime time2 = dto2.getLastSendMessageTime();

            // null 처리: 최신 메시지가 없는 경우 맨 뒤로 보냄
            if (time1 == null && time2 == null) return 0;
            if (time1 == null) return 1;
            if (time2 == null) return -1;

            return time2.compareTo(time1); // 최신 순 정렬 (내림차순)
        });

        return resultList;

    }

    //채팅방 아이디로 채팅방 정보 조회
    public MatchDTO.MathchingSucessResponse getChatRoomInfoByChatRoomId(Long userId, Long chatRoomId){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        MatchDTO.MathchingSucessResponse response = new MatchDTO.MathchingSucessResponse();
        response.setChatRoomId(chatRoomId);

        User partner = chatRoom.getUser1().getId() == user.getId() ? chatRoom.getUser2() : chatRoom.getUser1();

        response.setPartnerId(partner.getId());
        response.setPartnerNickname(partner.getNickname());
        response.setImgUrl(partner.getFace() == null ? null :partner.getFace().getImg());

        Matching matching = matchingRepository.findByRequesterAndAccepter(user , partner);

        response.setSimilar(matching.getCompatibility().doubleValue());

        return response;

    }
    private MessageDTO.ChatSummaryResponse getLastMessageAndUnreadCount(Long roomId, Long partnerId) {
        // DB에 메시지가 하나도 없다면 null 반환
        long messageCountInDB = messageRepository.countByRoom_Id(roomId);
        if (messageCountInDB == 0) {
            return null;
        }

        // 안 읽은 메시지 수 조회
        long unreadCountFromDB = messageRepository.countByRoom_IdAndSender_IdAndIsReadFalse(roomId, partnerId);
        System.out.println("DB에서 안 읽은 메시지 수: " + unreadCountFromDB);

        // 마지막 메시지 조회
        Message message = messageRepository.findTopByRoom_IdOrderBySendAtDesc(roomId)
                .orElseThrow(() -> new IllegalStateException("메시지가 존재해야 하는데 조회되지 않음"));


        // Message → MessageSendResponse 변환 (objectMapper 사용 시)
//        MessageDTO.MessageSendResponse lastMessage = objectMapper.convertValue(message, MessageDTO.MessageSendResponse.class);

        return new MessageDTO.ChatSummaryResponse(message.getContent(), message.getSendAt(),unreadCountFromDB);
    }






    //사용자가 해당 채팅방에 좋아요 또는 싫어요를 누름
    public void selectedLike(Long roomId, Long userId, boolean isLiked){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // user1과 user2 중 해당 사용자 찾기
        if (user.getId().equals(chatRoom.getUser1().getId())) {
            chatRoom.setUser1Selected(isLiked);
            chatRoom.setUser1SelectedAt(LocalDateTime.now());
        } else if (user.getId().equals(chatRoom.getUser2().getId())) {
            chatRoom.setUser2Selected(isLiked);
            chatRoom.setUser2SelectedAt(LocalDateTime.now());
        } else {
            throw new IllegalArgumentException("해당 채팅방에 속한 유저가 아닙니다.");
        }

        chatRoomRepository.save(chatRoom);
    }

    //해당 유저가 해당 채팅방을 나갑니다.
    public void leaveChatRoom(Long roomId, Long userId){
        System.out.println("나가려고 하는데 =====================");
        System.out.println("roomId" + roomId + "userId" + userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));



        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByUserAndRoom_Id(user, roomId);


        chatRoomMember.setIsVisible(false);
        chatRoomMember.setLeftAt(LocalDateTime.now());

        chatRoomMemberRepository.save(chatRoomMember);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomMember.getRoom().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

        //상대방 찾기
        User partner = chatRoom.getUser1().getId() == userId ? chatRoom.getUser2() : chatRoom.getUser1();

        userBlockService.createBlock(user.getId(),partner.getId());

    }

    /**
     * 사용자의 채팅방 캐시 무효화
     */
    private void invalidateUserChatCache(Long userId) {

        String listKey = CHAT_LIST_KEY + userId;


        redisTemplate.delete(listKey);


    }

}
