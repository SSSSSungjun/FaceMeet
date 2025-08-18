package com.levelup.FaceMeet.repository.fcm;

import com.levelup.FaceMeet.domain.fcm.UserNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserNotificationHistoryRepository extends JpaRepository<UserNotificationHistory, Long> {

    // 특정 유저의 모든 알림 히스토리 조회
    List<UserNotificationHistory> findAllByUser_Id(Long userId);

    // 특정 유저의 안읽은 메시지 개수 조회
    Long countByUser_IdAndIsReadFalse(Long userId);

    // ================= 현재 시간 이전만 =================
    // 특정 유저의 모든 알림 히스토리 조회 (현재 시간 이전만)
    List<UserNotificationHistory> findAllByUser_IdAndReceivedAtBefore(Long userId, LocalDateTime currentTime);

    List<UserNotificationHistory> findAllByUser_IdAndReceivedAtBeforeOrderByScheduledMessage_ScheduledTimeDesc(Long userId, LocalDateTime currentTime);

    // 특정 유저의 안읽은 메시지 개수 조회 (현재 시간 이전만)
    Long countByUser_IdAndIsReadFalseAndReceivedAtBefore(Long userId, LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE UserNotificationHistory u SET u.isRead = true WHERE u.user.id = :userId AND u.scheduledMessage.id = :scheduledMessageId")
    int markAsRead(@Param("userId") Long userId, @Param("scheduledMessageId") Long scheduledMessageId);

    boolean existsByUser_IdAndScheduledMessage_Id(Long userId, Long scheduledMessageId);
}