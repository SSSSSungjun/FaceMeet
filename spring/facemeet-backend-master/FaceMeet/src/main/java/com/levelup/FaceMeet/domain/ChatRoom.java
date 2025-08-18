package com.levelup.FaceMeet.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@ToString
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ChatRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    //좋아요 했을 경우 true
    @Column(name = "user1_selected", nullable = true)
    private Boolean user1Selected;

    //좋아요 했을 경우 true
    @Column(name = "user2_selected", nullable = true)
    private Boolean user2Selected;

    @Column(name = "user1_selected_at")
    private LocalDateTime user1SelectedAt;
    @Column(name = "user2_selected_at")
    private LocalDateTime user2SelectedAt;

    private String roomStringId;
}
