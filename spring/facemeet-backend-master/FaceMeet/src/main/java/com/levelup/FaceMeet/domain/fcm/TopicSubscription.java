package com.levelup.FaceMeet.domain.fcm;

import com.levelup.FaceMeet.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "topic_subscription",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "fcm_topic_id"}))
public class TopicSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_subscription_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "fcm_topic_id", nullable = false)
    private Long fcmTopicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fcm_topic_id", insertable = false, updatable = false)
    private FcmTopic fcmTopic;
}