package com.levelup.FaceMeet.repository.fcm;

import com.levelup.FaceMeet.domain.fcm.TopicSubscription;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TopicSubscriptionRepository extends JpaRepository<TopicSubscription, Long> {

    List<TopicSubscription> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    List<TopicSubscription> findByFcmTopicId(Long fcmTopicId);

    Optional<TopicSubscription> findByUserIdAndFcmTopicId(Long userId, Long topicId);

    boolean existsByUserIdAndFcmTopicId(Long userId, Long topicId);

    void deleteByUserIdAndFcmTopicId(Long userId, Long topicId);

    long countByFcmTopicId(Long fcmTopicId);

    @Query("SELECT DISTINCT ts.fcmTopicId FROM TopicSubscription ts")
    List<Long> findAllDistinctFcmTopicIds();

    // 특정 토픽 ID에 해당하는 모든 구독 정보를 삭제하는 쿼리
    void deleteAllByFcmTopicId(Long fcmTopicId);

    // 특정 토픽 ID를 구독하는 모든 사용자의 모든 활성 FCM 토큰을 조회하는 쿼리
    @Query("SELECT t.deviceToken FROM TopicSubscription ts JOIN FcmToken t ON ts.userId = t.userId WHERE ts.fcmTopicId = :topicId AND t.isActive = true")
    List<String> findAllTokensByTopicId(@Param("topicId") Long topicId);

    // JOIN FETCH를 사용하여 N+1 문제 해결
    @Query("SELECT ts FROM TopicSubscription ts JOIN FETCH ts.fcmTopic WHERE ts.userId = :userId")
    List<TopicSubscription> findByUserIdWithTopic(@Param("userId") Long userId);

    @Query("""
    SELECT CASE WHEN COUNT(ts) > 0 THEN true ELSE false END
    FROM TopicSubscription ts
    JOIN ts.fcmTopic t
    WHERE ts.userId = :userId 
    AND t.name = :topicName 
    AND t.isActive = true
    """)
    Boolean isUserSubscribedToTopic(@Param("userId") Long userId,
                                    @Param("topicName") String topicName);
}
