package com.observeai.platform.realtime.commons.data.messages.notifications;

import com.observeai.platform.realtime.commons.data.enums.CallTopicMessageType;
import lombok.Getter;

@Getter
public enum CallEventType {
    START_EVENT("Call has started"),
    END_EVENT("Call has ended"),
    MONITORING_START_EVENT("Monitoring call has started"),
    MONITORING_END_EVENT("Monitoring call has ended");

    public CallTopicMessageType getCallTopicMessageType() {
        switch (this) {
            case MONITORING_START_EVENT:
                return CallTopicMessageType.MONITORING_CALL_START_NOTIFICATION;
            case START_EVENT:
                return CallTopicMessageType.CALL_START_NOTIFICATION;
            case END_EVENT:
                return CallTopicMessageType.CALL_END_NOTIFICATION;
            case MONITORING_END_EVENT:
                return CallTopicMessageType.MONITORING_CALL_END_NOTIFICATION;
            default:
                return null;
        }
    }

    private String message;

    CallEventType(String message) {
        this.message = message;
    }
}
