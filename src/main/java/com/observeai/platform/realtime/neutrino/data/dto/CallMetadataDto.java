package com.observeai.platform.realtime.neutrino.data.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CallMetadataDto {
    private String callId;
    private String vendorAccountId;
    private String observeAccountId;
    private boolean recordAudio;
    private String agentId;
    private String observeUserId;
    private Long startTime;
    private String vendor;
    private Integer agentParticipantId;
    private Object callDetails;
    private Object agentDetails;
}
