package com.levelup.FaceMeet.repository.fcm;

import com.levelup.FaceMeet.domain.fcm.FcmTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmTopicRepository extends JpaRepository<FcmTopic, Long> {

    Optional<FcmTopic> findByFcmTopicId(Long topicId);
    Optional<FcmTopic> findByName(String name);
}
