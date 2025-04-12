package com.observeai.platform.realtime.neutrino.data.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class NiceEventDto {
    private String event;
    private String accountId;
    private String contactId;
    private String masterId;
    private String agentId;
    private String callDirection;

    // Observe details
    private String observeAccountId;
    private String observeUserId;
    private CallDirection direction;

    // Additional parameters from NICE, should be json string in pascal case
    private String additionalParams;
}
