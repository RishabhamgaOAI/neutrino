package com.observeai.platform.realtime.commons.data.messages;

import com.observeai.platform.realtime.commons.data.enums.AgentAssistMessageType;
import com.observeai.platform.realtime.commons.data.enums.CallTopicMessageType;
import com.observeai.platform.realtime.commons.data.enums.TimestampEvent;
import com.observeai.platform.realtime.commons.data.messages.details.AgentAssistMessage;
import com.observeai.platform.realtime.commons.data.messages.details.DeepgramMessage;
import com.observeai.platform.realtime.commons.data.messages.details.ProcessorResponseTimestampMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@NoArgsConstructor
public class DeepgramTranscriptResponse extends CallTopicMessage {
    private DeepgramMessage message;
    private AgentAssistMessage agentAssistMessage;
    private ProcessorResponseTimestampMessage timestampMessage;

    @Builder
    public DeepgramTranscriptResponse(String callId, String masterCallId, String parentCallId, String accountId, String userId,
                                      DeepgramMessage message, AgentAssistMessage agentAssistMessage, ProcessorResponseTimestampMessage timestampMessage) {
        super(callId, masterCallId, parentCallId, accountId, userId, CallTopicMessageType.DG_RESPONSE);
        this.message = message;
        this.agentAssistMessage = agentAssistMessage;
        this.timestampMessage = timestampMessage;
    }

    public static DeepgramTranscriptResponse fromDeepgramMessage(String callId, String accountId, String userId,
                                                                 long seqNum, long timestamp, DeepgramMessage deepgramMessage) {
        ProcessorResponseTimestampMessage timestampMessage =  ProcessorResponseTimestampMessage.builder()
                    .sequenceNum(seqNum)
                    .eventName(StringUtils.hasLength(deepgramMessage.getDefaultTranscript())
                            ? TimestampEvent.DG_TRANSCRIPT_RESPONSE : TimestampEvent.DG_SILENCE_RESPONSE)
                    .eventTimestamp(timestamp)
                    .startTimestamp(timestamp)
                    .speaker(deepgramMessage.getSpeaker())
                    .transcriptStart(deepgramMessage.getStart())
                    .transcriptDuration(deepgramMessage.getDuration())
                    .build();

        return DeepgramTranscriptResponse.builder().callId(callId).accountId(accountId).userId(userId)
                .message(deepgramMessage).agentAssistMessage(getAgentAssistMessage(deepgramMessage, timestamp, seqNum))
                .timestampMessage(timestampMessage).build();
    }

    private static AgentAssistMessage getAgentAssistMessage(DeepgramMessage deepgramMessage, long timestamp, long seqNum){
        if (deepgramMessage.getDefaultTranscript().equals("")) {
            return null;
        }

        return AgentAssistMessage.builder()
                .messageType(AgentAssistMessageType.TRANSCRIPT)
                .transcript(deepgramMessage.getDefaultTranscript())
                .speaker(deepgramMessage.getSpeaker())
                .timestamp(timestamp)
                .dgSeqNum(seqNum)
                .dgFinalFlag(deepgramMessage.isFinal())
                .build();
    }
}
