package com.observeai.platform.realtime.neutrino.data.dto;

import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class CallEndEventDto {
    private String accountId;
    private String callId;
    private String partnerMeetingId;
    private String agentId;
    private String observeCallId;
    private CallDirection callDirection;
    private Boolean recordAudio;
    private Long startTime;
    private Long endTime;
    private boolean danglingCall;
    private List<CallEventDto> callEvents;
    private List<CallBackMetaEventDto> callBackMetaEvents;
}
