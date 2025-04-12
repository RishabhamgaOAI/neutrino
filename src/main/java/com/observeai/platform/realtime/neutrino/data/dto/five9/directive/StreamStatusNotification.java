package com.observeai.platform.realtime.neutrino.data.dto.five9.directive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamStatusNotification {
    private String callId;
    private String domainId;
    private String callLeg;
    private String status;
    private String failureType;
    private String failureDetail;
    private String destinationUrl;
    private String occurredOn;
}
