package com.levelup.FaceMeet.controller.chat;

import com.levelup.FaceMeet.dto.ChatRoomDTO;
import com.levelup.FaceMeet.dto.MessageDTO;
import com.levelup.FaceMeet.dto.UserInfoDTO;
import com.levelup.FaceMeet.service.chat.ChatRoomService;
import com.levelup.FaceMeet.service.chat.ChatService;
import com.levelup.FaceMeet.service.chat.SessionUserService;
import com.levelup.FaceMeet.service.fcm.FcmMessageService;
import com.levelup.FaceMeet.service.user.UserBlockService;
import com.levelup.FaceMeet.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class WebSocketController {


    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private SessionUserService sessionUserService;

    @Autowired
    private UserBlockService userBlockService;

    @Autowired
    private FcmMessageService fcmMessageService;


    @Autowired
    private UserService userService;

    @Autowired
    private ChatRoomService chatRoomService;

    //1대1 메시지 전송
    //웹 소켓에서 클라이언트가 보낸 메시지를 처리
    @MessageMapping("/chat.private")
    public void sendMessage(@Payload MessageDTO.MessageSaveRequest req, SimpMessageHeaderAccessor headerAccessor){
        System.out.println("======================================메시지 전송중에서 사용자 확인중 =====================================");
        System.out.println("senderId : " +req.getSenderId() +" " + "receiverId : "+req.getReceiverId() +" "+ "content : "+req.getContent() );

        UserInfoDTO.UserInfoResponse sender = userService.getUserInfo(req.getSenderId());
        UserInfoDTO.UserInfoResponse receiver = userService.getUserInfo(req.getReceiverId());

        boolean isBlocked = userBlockService.isBlockedByPartner(req.getReceiverId(), req.getSenderId());
        //추후 삭제하겠습니다. 무조검 알림 보냄
        fcmMessageService.sendChatMessageByUserId(sender.getNickname() , req.getContent(), req.getRoomId(), req.getReceiverId());


        if (isBlocked) {
            // 송신자에게 차단 메시지 전송 (프론트에서 처리할 수 있도록)
            messagingTemplate.convertAndSend(
                    "/sub/private/" + req.getSenderId(),
                    Map.of(
                            "type", "BLOCKED",
                            "message", "상대방이 당신을 차단하여 메시지를 보낼 수 없습니다.",
                            "receiverId", req.getReceiverId()
                    )
            );

            System.out.println("차단 상태이므로 메시지를 저장하거나 전송하지 않습니다.");

        }

        else{
            System.out.println("메시지 전송 중입니다 : "+req.getContent());
            System.out.println(req.getRoomId());
            System.out.println(req.getContent());
            System.out.println(req.getSenderId());
            System.out.println(req.getReceiverId());
            System.out.println("-------------------------------");

            //메시지를 디비에 저장
            MessageDTO.MessageSendResponse savedMessage = chatService.sendMessage(req);


            //받는 사람이 온라인인지 확인
            boolean isReceiverOnline = sessionUserService.isUserConnected(req.getReceiverId().toString());


            if(isReceiverOnline){


                //수신자에게 실시간 메시지 전송
                messagingTemplate.convertAndSend(
                        "/sub/private/" + req.getReceiverId(),
                        savedMessage
                );
                System.out.println(savedMessage);


            }

            //여기 else일 경우 fcm 알림 보내는 기능 추가
            else{
                System.out.println("현재 해당 사용자가 연결되어 있지 않습니다.");

//                fcmMessageService.sendChatMessageByUserId(sender.getNickname() , req.getContent(), req.getRoomId(), req.getReceiverId());

            }
            //송신자에게 전송 확인 메시지
            messagingTemplate.convertAndSend(
                    "/sub/private/" + req.getSenderId(),
                    savedMessage
            );
            System.out.println(savedMessage);
        }


    }

    //사용자 온라인 상태 등록
    @MessageMapping("/chat.connect")
    public void registerUser(@Payload String userIdFromClient, SimpMessageHeaderAccessor headerAccessor) {
        String stompSessionId = headerAccessor.getSessionId(); // 현재 웹소켓 연결의 STOMP 세션 ID

        System.out.println("--- ChatController: registerUser 호출됨 ---");
        System.out.println("클라이언트로부터 받은 userId (Payload): " + userIdFromClient);
        System.out.println("현재 STOMP 세션 ID: " + stompSessionId);

        if (userIdFromClient != null && !userIdFromClient.trim().isEmpty()) {
            sessionUserService.addUserSession(userIdFromClient, stompSessionId); // 사용자 ID와 세션 ID 매핑
            System.out.println("SessionUserService에 사용자 '" + userIdFromClient + "' (세션 ID: " + stompSessionId + ") 등록 완료.");

            // (선택 사항) 클라이언트에게 등록 성공 메시지 보냄
            messagingTemplate.convertAndSendToUser(
                    userIdFromClient,
                    "/sub/private/" + userIdFromClient,
                    "서버에 연결 및 등록되었습니다. 당신의 ID는 " + userIdFromClient + " 입니다."
            );
        } else {
            System.err.println("오류: 클라이언트로부터 유효한 사용자 ID를 받지 못했습니다. 등록 실패.");
        }
        System.out.println("------------------------------------");
    }


    //메시지 읽음 처리
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload MessageDTO.MessageReadRequest req){
        chatService.markMessagesAsRead(req.getReaderId(),  req.getRoomId());

        String currentTime = LocalDateTime.now().toString();
        System.out.println("소캣쪽에서 읽음 처리 시도중");

        // 1. 읽음 처리를 시도한 사람에게 성공 응답
        messagingTemplate.convertAndSend(

                "/queue/read-success",
                Map.of(
                        "type", "READ_SUCCESS",
                        "roomId", req.getRoomId(),
                        "readAt", currentTime,
                        "message", "메시지가 읽음 처리되었습니다."
                )
        );

        // 2. 송신자에게 읽음 확인 알림
        messagingTemplate.convertAndSend(

                "/sub/private/" + req.getSenderId(),
                Map.of(
                        "type", "READ_RECEIPT",
                        "roomId", req.getRoomId(),
                        "readerId", req.getReaderId(),
                        "readAt", currentTime,
                        "message", "상대방이 메시지를 읽었습니다."
                )
        );

        System.out.println("소캣쪽에서 읽음 처리 완료");
    }

    //채팅방 나가기 처리
    @MessageMapping("/chat.leave")
    public void leaveChatRoom(@Payload ChatRoomDTO.ChatLeaveRequest req){
        System.out.println("===============채팅방 나가기 처리 ===================");
        System.out.println("사용자 ID: " + req.getUserId() + ", 방 ID: " + req.getRoomId() + ", 상대방 ID: " + req.getPartnerId());

        try{
            //채팅방 나기기
            chatRoomService.leaveChatRoom(req.getRoomId() , req.getUserId());
            // 상대방이 온라인인지 확인
            boolean isPartnerOnline = sessionUserService.isUserConnected(req.getPartnerId().toString());
            UserInfoDTO.UserInfoResponse  leavingUser = userService.getUserInfo(req.getUserId());

            if(isPartnerOnline){
                //상대방에게 나가기 알림 전송
                Map<String, Object> leaveNotification =  Map.of(
                        "type", "USER_LEFT",
                        "roomId" , req.getRoomId(),
                        "userId" , req.getUserId(),
                        "userName" ,  leavingUser.getName(),
                        "message" , leavingUser.getNickname() +"님이 채팅방을 나갔습니다." ,
                        "leftAt" , LocalDateTime.now().toString()
                );

                //수신자에게 실시간 메시지 전송
                messagingTemplate.convertAndSend(
                        "/sub/private/" + req.getPartnerId(),
                        leaveNotification
                );
                System.out.println("상대방에게 실시간 나가기 알림 전송 완료");
            }else{
                System.out.println("상대방이 오프라인 상태 입니다.");
            }

            //나가는 사용자에게 확인 메시지 전송
            messagingTemplate.convertAndSend(
                    "/sub/private/" +req.getUserId(),
                    Map.of(
                            "type", "LEAVE_CONFIRMED",
                            "roomId", req.getRoomId(),
                            "message", "채팅방을 나갔습니다."

                    )
            );
            System.out.println("채팅방 나가기 처리 완료");

        }catch(Exception e){
            System.err.println("채팅방 나가기 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();;

            //오류 발생시 사용자에게 오류 메시지 전송
            messagingTemplate.convertAndSend(
                    "/sub/private/" + req.getUserId(),
                    Map.of("type" , "LEAVE_ERROR" , "message" , "채팅방 나가기 처리 중 오류가 발생했습니다.")

            );
        }
    }


}
