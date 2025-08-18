package com.levelup.FaceMeet.repository.chat;

import com.levelup.FaceMeet.domain.ChatRoom;
import com.levelup.FaceMeet.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository  extends JpaRepository<ChatRoom, Long>  {
    ChatRoom findByRoomStringId(String roomStringId);

    boolean existsByUser1AndUser2(User user1, User user2);



}
