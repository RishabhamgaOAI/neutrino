package com.observeai.platform.realtime.commons.data.messages.details;

import com.observeai.platform.realtime.commons.data.enums.AgentAssistMessageType;
import com.observeai.platform.realtime.neutrino.enums.Speaker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentAssistMessage {
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String transcript;
    private long timestamp;
    private Speaker speaker;
    private AgentAssistMessageType messageType;
    private List<Map<String, String>> momentNames;
    private List<String> suggestions;
    private Long dgSeqNum;
    private Boolean dgFinalFlag;
}
