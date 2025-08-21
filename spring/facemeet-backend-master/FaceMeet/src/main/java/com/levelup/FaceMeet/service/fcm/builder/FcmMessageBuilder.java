package com.levelup.FaceMeet.service.fcm.builder;

import com.levelup.FaceMeet.config.fcm.FcmConstants;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * FCM 메시지 데이터 빌더
 */
@Component
public class FcmMessageBuilder {

    /**
     * 기본 알림 메시지 데이터 생성
     */
    public Map<String, String> buildBasicData(String title, String body, Map<String, String> customData, String type) {
        Map<String, String> data = new HashMap<>();
        data.put(FcmConstants.DATA_TITLE, title);
        data.put(FcmConstants.DATA_BODY, body);
        data.put(FcmConstants.DATA_TYPE, type);
        data.put(FcmConstants.DATA_TIMESTAMP, LocalDateTime.now().toString());
        if(customData != null) data.putAll(customData);
        return data;
    }

    /**
     * 예약 알림 메시지 데이터 생성
     */
    public Map<String, String> buildScheduledData(String title, String body, Map<String, String> customData,
                                                  Long settingId, LocalDateTime triggerTime) {
        Map<String, String> data = buildBasicData(title, body, customData, FcmConstants.MessageType.SCHEDULED_EVENT);
        data.put(FcmConstants.DATA_SETTING_ID, String.valueOf(settingId));
        data.put(FcmConstants.DATA_TRIGGER_TIME, triggerTime.toString());
        return data;
    }

    /**
     * 사전 알림 메시지 데이터 생성
     */
    public Map<String, String> buildPreMessageData(String title, String body, Map<String, String> customData, Long settingId) {
        Map<String, String> data = buildBasicData(title, body, customData, FcmConstants.MessageType.PRE_MESSAGE);
        data.put(FcmConstants.DATA_SETTING_ID, String.valueOf(settingId));
        return data;
    }

    /**
     * 채팅 알림 메시지 데이터 생성
     */
    public Map<String, String> buildChatData(String title, String body, Long roomId) {
        Map<String, String> data = buildBasicData(title, body, null, FcmConstants.MessageType.CHAT);
        data.put(FcmConstants.DATA_ROOM_ID, String.valueOf(roomId));
        return data;
    }
}