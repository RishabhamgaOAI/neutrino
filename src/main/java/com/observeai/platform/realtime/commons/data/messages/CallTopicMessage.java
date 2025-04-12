package com.observeai.platform.realtime.commons.data.messages;

import com.observeai.platform.realtime.commons.data.enums.CallTopicMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallTopicMessage {
    private String id;
    private String callId;
    private String masterCallId;
    private String parentCallId;
    private String accountId;
    private String userId;
    private CallTopicMessageType type;

    public CallTopicMessage(String callId, String masterCallId, String parentCallId, String accountId, String userId, CallTopicMessageType messageType) {
        this.id = UUID.randomUUID().toString();
        this.callId = callId;
        this.masterCallId = masterCallId;
        this.parentCallId = parentCallId;
        this.accountId = accountId;
        this.userId = userId;
        this.type = messageType;
    }
}
