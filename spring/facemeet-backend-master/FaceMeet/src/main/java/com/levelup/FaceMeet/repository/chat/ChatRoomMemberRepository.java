package com.levelup.FaceMeet.repository.chat;

import com.levelup.FaceMeet.domain.ChatRoomMember;
import com.levelup.FaceMeet.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomMemberRepository  extends JpaRepository<ChatRoomMember, Long> {
    //특정 사용자의 나가지 않은 모든 채팅방 목록
    List<ChatRoomMember> findByUserAndIsVisible(User user, boolean isVisible);

    //특정 사용자가 나가지 않은 채팅방 목록을 user의 id로 조회한다.
    List<ChatRoomMember> findByUserIdAndIsVisible(Long userId, boolean isVisible);
    //사용자의 해당 채팅방 번호 찾기
    ChatRoomMember findByUserAndRoom_Id(User user, Long roomId);
}

