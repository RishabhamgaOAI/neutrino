package com.observeai.platform.realtime.commons.data.messages;

import com.observeai.platform.realtime.commons.data.enums.CallTopicMessageType;
import com.observeai.platform.realtime.commons.data.messages.details.AudioMessage;
import com.observeai.platform.realtime.commons.data.messages.details.TimestampMessage;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CallAudioMessage extends CallTopicMessage {
    private AudioMessage audioMessage;
    private TimestampMessage timestampMessage;

    @Builder
    public CallAudioMessage(String callId, String masterCallId, String parentCallId, String accountId, String userId, AudioMessage audioMessage, TimestampMessage timestampMessage) {
        super(callId, masterCallId, parentCallId, accountId, userId, CallTopicMessageType.CALL_AUDIO_PACKET);
        this.audioMessage = audioMessage;
        this.timestampMessage = timestampMessage;
    }
}
