package com.levelup.FaceMeet.repository.fcm;

import com.levelup.FaceMeet.domain.fcm.ScheduledMessage;
import org.springframework.data.jpa.repository.JpaRepository;

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
}

