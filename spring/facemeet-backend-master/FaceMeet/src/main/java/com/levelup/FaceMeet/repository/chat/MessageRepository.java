package com.levelup.FaceMeet.repository.chat;


import com.levelup.FaceMeet.domain.ChatRoom;
import com.levelup.FaceMeet.domain.Message;
import com.levelup.FaceMeet.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    //특정 채팅방의 모든 메시지 조회
    //2주 전까지만
    List<Message> findByRoomAndSendAtAfterOrderBySendAtDesc(ChatRoom room, LocalDateTime after);


    //해당 사용자가 해당 채팅방에서 받은 메시지 조회
    List<Message> findByReceiverAndRoomAndIsReadFalse(User receiver, ChatRoom room);

    // 안읽은 메시지 수 (room.id(Long), sender.id(Long))
    long countByRoom_IdAndSender_IdAndIsReadFalse(Long roomId, Long senderId);

    // 마지막 메시지 조회 (room.id(Long) 기준)
    Optional<Message> findTopByRoom_IdOrderBySendAtDesc(Long roomId);

    // roomId와 deleted 상태를 조건으로 메시지 조회
    Page<Message> findByRoom_Id(Long roomId, Pageable pageable);

    //특정 사용자가 받은 읽지 않은 메시지 개수

    //특정 채팅방에서 특정 사용자가 받은 읽지 않음 메시지들

    // 채팅방 ID, 수신자 ID, 읽지 않은 메시지를 기준으로 메시지를 조회
    List<Message> findByRoom_IdAndReceiver_IdAndIsReadFalse(Long roomId, Long receiverId);

    //현재 페이지 총 수
    long countByRoom_Id(Long roomId);

    //특정 채방방의 모든 메시지
    List<Message> findByRoom_IdOrderBySendAtAsc(Long roomId);
    long countByRoomId(Long roomId);


}
