package com.levelup.FaceMeet.domain.fcm;

import com.levelup.FaceMeet.domain.Setting;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "scheduled_message")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scheduled_message_id")
    private Long id;

    @Column(name = "setting_id")
    private Long settingId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", insertable = false, updatable = false)
    private Setting setting;

    @Column(name = "fcm_topic_id")
    private Long fcmTopicId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fcm_topic_id", insertable = false, updatable = false)
    private FcmTopic fcmTopic;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> messageData;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageStatus status = MessageStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MessageStatus {
        PENDING,
        SCHEDULED,
        SENT,
        FAILED,
        CANCELLED
    }
}