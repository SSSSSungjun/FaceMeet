//package com.levelup.FaceMeet.scheduler;
//
//import com.levelup.FaceMeet.domain.ChatRoom;
//import com.levelup.FaceMeet.domain.Message;
//import com.levelup.FaceMeet.domain.User;
//import com.levelup.FaceMeet.dto.MessageDTO;
//import com.levelup.FaceMeet.repository.chat.ChatRoomRepository;
//import com.levelup.FaceMeet.repository.chat.MessageRepository;
//import com.levelup.FaceMeet.repository.user.UserRepository;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Component
//public class ConsoleScheduler {
//
//    private static final String CHAT_ROOM_KEY_PREFIX = "chat:room:"; // Redis 키 프리픽스
//    private static final long BATCH_FETCH_SIZE = 1000; // 한 번에 Redis에서 가져올 메시지 최대 개수
//
//    @Autowired
//    private RedisTemplate<String, MessageDTO.MessageSendResponse> redisMessageSendResponse; // Redis에 저장된 DTO 타입에 맞춰야 함
//
//    @Autowired
//    private MessageRepository messageRepository;
//
//    @Autowired
//    private ChatRoomRepository chatRoomRepository;
//
//    @Autowired
//
//    private UserRepository userRepository;
//
//    @Scheduled(cron = "*/10 * * * * *") // 매 시의 30분마다
//    @Transactional
//    public void saveMessagesFromRedisToDb () {
//
//        //redis에 존재하는 모든 채팅방 키 가져오기
//        Set<String> chatRoomKeys = redisMessageSendResponse.keys(CHAT_ROOM_KEY_PREFIX  +"*" );
//        System.out.println("--- 스케줄러 : Redis 메시지를 db에 저장한다.");
//
//        if(chatRoomKeys == null || chatRoomKeys.isEmpty()){
//            System.out.println("Redis에 저장할 채팅 메시지가 없습니다");
//            return;
//        }
//
//        int totalSavedMessages = 0;
//
//        for(String roomKey : chatRoomKeys){
//
//            List<MessageDTO.MessageSendResponse> messagesFromRedis =
//                    redisMessageSendResponse.opsForList().range(roomKey, 0, BATCH_FETCH_SIZE - 1);
//
//            if (messagesFromRedis == null || messagesFromRedis.isEmpty()) {
//                continue;
//            }
//
//            List<Message> messagesToSave = new ArrayList<>();
//            List<MessageDTO.MessageSendResponse> processedMessages = new ArrayList<>();
//
//            for (MessageDTO.MessageSendResponse dto : messagesFromRedis){
//                System.out.println(dto.getContent());
//                System.out.println(dto.getSenderId());
//                System.out.println(dto.getReceiverId());
//                System.out.println(dto.getRoomId());
//                System.out.println(dto.getIsRead());
//                System.out.println(dto.getReadAt());
//                User sender = userRepository.findById(dto.getSenderId())  .orElseThrow(() -> new RuntimeException("해당 사용자가 존재 하지 않습니다 : " + dto.getSenderId()));
//
//                User receiver = userRepository.findById(dto.getReceiverId()).orElseThrow( () -> new RuntimeException("해당 사용자가 존재 하지 않습니다 :"+ dto.getReceiverId()));
//
//                ChatRoom chatRoom = chatRoomRepository.findByRoomStringId(dto.getRoomId());
//
//
//                System.out.println("-------------------");
//                Message message = new Message();
//                message.setSender(sender);
//                message.setReceiver(receiver);
//                message.setContent(dto.getContent());
//                message.setRoom(chatRoom);
//                message.setReadAt(dto.getReadAt());
//                message.setIsRead(dto.getIsRead());
//
//                messagesToSave.add(message);
//                processedMessages.add(dto);
//
//
//
//            }
//
//            List<Message> savedMessages = messageRepository.saveAll(messagesToSave);
//            totalSavedMessages += savedMessages.size();
//            redisMessageSendResponse.opsForList().trim(roomKey, messagesFromRedis.size(), -1);
//
//            System.out.println(roomKey + "에서 " + messagesFromRedis.size() + "개의 메시지를 DB에 저장하고 Redis에서 제거했습니다.");
//
//        }
//
//        System.out.println("--- 스케줄러: 총 " + totalSavedMessages + "개의 메시지를 DB에 저장 완료 ---");
//
//    }
//}
