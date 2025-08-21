package com.levelup.FaceMeet.config.fcm;

/**
 * FCM 관련 상수
 */
public final class FcmConstants {

    private FcmConstants() {
        // 인스턴스화 방지
    }

    /**
     * 메시지 타입
     */
    public static final class MessageType {
        public static final String IMMEDIATE_EVENT = "IMMEDIATE_EVENT";
        public static final String SCHEDULED_EVENT = "SCHEDULED_EVENT";
        public static final String PRE_MESSAGE = "PRE_MESSAGE";
        public static final String CHAT = "CHAT";

        private MessageType() {}
    }

    /**
     * 데이터 필드 키
     */
    public static final String DATA_TITLE = "title";
    public static final String DATA_BODY = "body";
    public static final String DATA_TYPE = "type";
    public static final String DATA_TIMESTAMP = "timestamp";
    public static final String DATA_SETTING_ID = "settingId";
    public static final String DATA_ROOM_ID = "roomId";
    public static final String DATA_TRIGGER_TIME = "triggerTime";
    public static final String DATA_EVENT_DATA = "eventData";

    /**
     * 캐시 이름
     */
    public static final String CACHE_USER_INFO = "user_info";

    /**
     * 배치 크기
     */
    public static final int MAX_MULTICAST_SIZE = 500;
}