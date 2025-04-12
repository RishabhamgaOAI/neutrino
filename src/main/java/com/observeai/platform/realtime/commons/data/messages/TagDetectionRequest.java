package com.observeai.platform.realtime.commons.data.messages;

import com.observeai.platform.realtime.commons.data.enums.CallTopicMessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class TagDetectionRequest extends CallTopicMessage {
    private TagDetectionMessage message;

    @Builder
    public TagDetectionRequest(String callId, String masterCallId, String parentCallId, String accountId, String userId, TagDetectionMessage message) {
        super( callId, masterCallId, parentCallId, accountId, userId, CallTopicMessageType.TAG_DETECTION_REQUEST);
        this.message = message;
    }
}
