package com.levelup.FaceMeet.repository.fcm;

import com.levelup.FaceMeet.domain.fcm.UserNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface UserNotificationHistoryRepository extends JpaRepository<UserNotificationHistory, Long> {

    // 특정 유저의 모든 알림 히스토리 조회 (현재 시간 이전만)
    List<UserNotificationHistory> findAllByUser_IdAndReceivedAtBeforeOrderByScheduledMessage_ScheduledTimeDesc(Long userId, LocalDateTime currentTime);

    // 특정 유저의 안읽은 메시지 개수 조회 (현재 시간 이전만)
    Long countByUser_IdAndIsReadFalseAndReceivedAtBefore(Long userId, LocalDateTime currentTime);

    // 읽음 처리
    @Modifying
    @Query("UPDATE UserNotificationHistory u SET u.isRead = true WHERE u.user.id = :userId AND u.scheduledMessage.id = :scheduledMessageId")
    int markAsRead(@Param("userId") Long userId, @Param("scheduledMessageId") Long scheduledMessageId);

    // 히스토리 조회
    boolean existsByUser_IdAndScheduledMessage_Id(Long userId, Long scheduledMessageId);

    // 메시지 목록 중 이미 수신된 메시지 필터링
    List<UserNotificationHistory> findByUser_IdAndScheduledMessage_IdIn(Long userId, List<Long> messageIds);

    // 히스토리 제거
    void deleteByUser_IdAndScheduledMessage_Id(Long userId, Long messageId);
}