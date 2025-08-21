package com.levelup.FaceMeet.config.fcm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * FCM 설정 속성 (application.yml 매핑)
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "event")
public class FcmProperties {

    private int preMessageMinutes;
    private int messageDispatchMinutes;
    private String defaultTopicName;
}