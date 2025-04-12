package com.observeai.platform.realtime.neutrino.data.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallBackMetaEventDto {
    //Vendor Details
    private String vendorName;
    private String vendorCallId;
    private String vendorAccountId;
    private String vendorAgentId;

    //Observe Details
    private CallBackMetaEventType callEventType;
    private String observeAccountId;
    private String observeUserId;
    private CallDirection direction;
    private Long arrivalTimestamp;

    Map<String, String> eventMetadata;
}
