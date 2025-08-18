package com.levelup.FaceMeet.controller.chat;


import com.levelup.FaceMeet.domain.ChatRoom;
import com.levelup.FaceMeet.dto.ChatRoomDTO;
import com.levelup.FaceMeet.dto.MatchDTO;
import com.levelup.FaceMeet.dto.MessageDTO;
import com.levelup.FaceMeet.security.dto.CustomUserDetails;
import com.levelup.FaceMeet.service.chat.ChatRoomService;
import com.levelup.FaceMeet.service.chat.ChatService;

import com.levelup.FaceMeet.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chatrooms")
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private UserService userService;

    @Autowired
    private ChatRoomService chatRoomService;



//    @PostMapping("")
//    @Operation(summary = "새로운 채팅방 생성", description = "유저 2명 입력받아서 새로운 채팅방 생성")
//    public ResponseEntity<ChatRoom> createChatRoom(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody ChatRoomDTO.CreateChatRoomRequest req) {
//        //새로운 chatroom 생성 (db)
//        ChatRoom chatRoom = chatService.getOrCreateChatRoom(customUserDetails.getUserId(), req.getUser2Id());
//
//        return ResponseEntity.ok(chatRoom);
//
//    }

    @GetMapping("/{roomid}")
    @Operation(summary = "채팅방 정보 조회", description = "채팅방 아이디로 채팅방 정보를 조회합니다")
    public ResponseEntity<MatchDTO.MathchingSucessResponse> getChatRoomInfoByRoomId(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long roomid){
        MatchDTO.MathchingSucessResponse response = chatRoomService.getChatRoomInfoByChatRoomId(customUserDetails.getUserId(), roomid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    @Operation(summary = "유저별 채팅방 리스트", description = "유저별로 갖고있는 채팅방 리스트를 조회합니다 blocked는 내가 차단하거나 당했을경우 false로 보내집니다")
    public ResponseEntity<List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse>> getChatRoomListByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        long start = System.currentTimeMillis(); // 시작 시간 측정

        List<ChatRoomDTO.ChatRoomMemberAndUserAndMessageResponse> chatList =
                chatRoomService.chatListByUser(userDetails.getUserId());

        long end = System.currentTimeMillis(); // 종료 시간 측정
        long duration = end - start; // 실행 시간 계산 (ms 단위)

        System.out.println("[DEBUG] 채팅방 리스트 조회 수행 시간: " + duration + " ms");

        return ResponseEntity.ok(chatList);
    }

    //
//    //채팅 읽음
    @PostMapping("/read/{roomid}")
    @Operation(summary = "채팅 읽음", description = "유저가 해당 채팅방에 들어갈 경우 해당 유저가 해당 채팅방의 대화 내용을 모두 읽음 표시합니다. ")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable Long roomid, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        chatService.markMessagesAsRead(customUserDetails.getUserId(), roomid);
        return ResponseEntity.ok().build();
    }
//
//    //채팅방 대화 내역
    @GetMapping("/{roomId}/messages/last")
    @Operation(summary = "채팅방 마지막 페이지 대화 내역 조회", description = "채팅방의 모든 대화 내역 중 마지막 페이지의 대화 내역을 보내줍니다")
    public ResponseEntity<MessageDTO.AllMessageResponse> getLastPageMessages(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "30") int limit // 쿼리 파라미터로 limit 받기, 기본값 20
    ) {
        MessageDTO.AllMessageResponse messages = chatService.getLastPageMessages(customUserDetails.getUserId(),  limit, roomId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{roomId}/messages")
    @Operation(summary = "채팅방 현재 페이지 조회", description = "채팅방의 현재 페이지의 대화 내역을 조회합니다")
    public ResponseEntity<MessageDTO.AllMessageResponse> getLastPageMessages(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "30") int limit ,// 쿼리 파라미터로 limit 받기, 기본값 20
            @RequestParam(defaultValue = "1") int page
    ) {
        MessageDTO.AllMessageResponse messages = chatService.getPageMessages(customUserDetails.getUserId(), page , limit, roomId);
        return ResponseEntity.ok(messages);
    }

//    @GetMapping("/{roomId}/messages/all")
//    @Operation(summary = "채팅방 모든 대화 내역 조회(임시)", description = "채팅방의 모든 대화 내역을 보내줍니다")
//    public ResponseEntity<MessageDTO.AllMessageResponse> getAllMessages(
//            @AuthenticationPrincipal CustomUserDetails customUserDetails,
//            @PathVariable Long roomId
//    ) {
//        MessageDTO.AllMessageResponse messages = chatService.getAllMessages(customUserDetails.getUserId() , roomId);
//        return ResponseEntity.ok(messages);
//    }

    //
//    //해당 사용자의 좋아요 , 싫어요 누름
    @PostMapping("/{roomId}")
    @Operation(summary = "채팅방 좋아요 싫어요 ", description = "좋아요를 누를 경우 (true) , 싫어요를 누를경우 (false)로 판단됩니다")
    public ResponseEntity<Void> selectedLiked(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long roomId, @RequestParam("selected") boolean selected) {

        chatRoomService.selectedLike(roomId, userDetails.getUserId(), selected);
        return ResponseEntity.ok().build();
    }

    //
//    //해당 사용자의 채팅방 나가기
    @PostMapping("/{roomId}/leave")
    @Operation(summary = "채팅방 나가기", description = "해당 사용자가 채팅방을 나갑니다 . 채팅방 삭제 x")
    public ResponseEntity<Void> leaveRoom(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long roomId) {
        chatRoomService.leaveChatRoom(roomId, customUserDetails.getUserId());
        return ResponseEntity.ok().build();
    }


}
