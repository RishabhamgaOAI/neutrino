package com.observeai.platform.realtime.neutrino.data;

import lombok.Getter;

@Getter
public enum CallState {
    INIT(0),
    CONNECTION_ESTABLISHED(1),
    STARTED(2),
    SECONDARY_STREAM_STARTED(2),
    ACTIVE_PROCESSING(3),
    ENDED_FOR_TRANSFER(4),
    SECONDARY_STREAM_ENDED(5),
    ENDED(6);

    private final int value;

    CallState(int value) {
        this.value = value;
    }
}
