package com.levelup.FaceMeet.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreMessagePreparedEvent {
    private final Long messageId;
}