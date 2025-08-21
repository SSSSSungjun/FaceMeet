package com.levelup.FaceMeet.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FcmMessageSentEvent {
    private final Long messageId;
    private final Long topicId;
    private final boolean success;
    private final String errorMessage;

    // 성공 이벤트 생성
    public static FcmMessageSentEvent success(Long messageId, Long topicId) {
        return new FcmMessageSentEvent(messageId, topicId, true, null);
    }

    // 실패 이벤트 생성
    public static FcmMessageSentEvent failure(Long messageId, Long topicId, String errorMessage) {
        return new FcmMessageSentEvent(messageId, topicId, false, errorMessage);
    }
}