package com.levelup.FaceMeet.repository.fcm;

import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledMessageRepository extends JpaRepository<ScheduledMessage, Long> {
    List<ScheduledMessage> findByStatus(ScheduledMessage.MessageStatus status);

    List<ScheduledMessage> findByScheduledTimeBetween(LocalDateTime start, LocalDateTime end);

    List<ScheduledMessage> findBySettingIdAndStatusIn(Long settingId, List<ScheduledMessage.MessageStatus> statuses);

    boolean existsBySettingIdAndStatusIn(Long settingId, List<ScheduledMessage.MessageStatus> sent);

    List<ScheduledMessage> findByStatusAndScheduledTimeAfter(ScheduledMessage.MessageStatus status, LocalDateTime now);

    List<ScheduledMessage> findByStatusAndScheduledTimeBefore(ScheduledMessage.MessageStatus status, LocalDateTime now);

    List<ScheduledMessage> findBySettingId(Long settingId);

    List<ScheduledMessage> findBySettingIdAndStatus(Long settingId, ScheduledMessage.MessageStatus status);

    @Modifying
    @Query("UPDATE ScheduledMessage sm SET sm.status = :status WHERE sm.settingId = :settingId")
    void updateStatusBySettingId(@Param("settingId") Long settingId,
                                 @Param("status") ScheduledMessage.MessageStatus status);

    @Modifying
    @Query("UPDATE ScheduledMessage sm SET sm.status = :status WHERE sm.id = :id")
    void updateStatusById(@Param("id") Long id,
                          @Param("status") ScheduledMessage.MessageStatus status);

    @Modifying
    @Query("UPDATE ScheduledMessage m SET m.status = :newStatus WHERE m.status = :oldStatus AND m.scheduledTime < :time")
    int bulkUpdateStatus(@Param("oldStatus") ScheduledMessage.MessageStatus oldStatus,
                         @Param("newStatus") ScheduledMessage.MessageStatus newStatus,
                         @Param("time") LocalDateTime time);
}

