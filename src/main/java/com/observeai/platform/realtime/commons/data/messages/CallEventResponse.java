package com.observeai.platform.realtime.commons.data.messages;

import com.observeai.platform.realtime.commons.data.enums.CallTopicMessageType;
import com.observeai.platform.realtime.commons.data.messages.details.CallEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CallEventResponse extends CallTopicMessage {
    private CallEvent message;

    @Builder
    public CallEventResponse(String callId, String masterCallId, String parentCallId, String accountId, String userId,
                             CallEvent message) {
        super(callId, masterCallId, parentCallId, accountId, userId, CallTopicMessageType.CALL_EVENT_RESPONSE);
        this.message = message;
    }
}
