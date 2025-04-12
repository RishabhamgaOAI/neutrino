package com.observeai.platform.realtime.neutrino.data.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class TalkdeskEventDto {
    private String eventType;
    private String accountId;
    private String callId;
    private String interactionId;
    private String callType;
    private String agentName;
    private String agentId;
    private String agentEmail;

    // Observe details
    private String observeAccountId;
    private String observeUserId;
    private CallDirection direction;
}




