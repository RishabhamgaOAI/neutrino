package com.observeai.platform.realtime.commons.data.messages.notifications;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CallEventDto {
    private long time;
    private CallEventType type;
    private String vendorCallId;
    private String partnerMeetingId;
    private String observeCallId;
    private String masterCallId;
    private String parentCallId;
    private String vendor;
    private String observeAccountId;
    private String observeUserId;
    private boolean recordAudio;
    private boolean supervisorAssistAudioEnabled;
    private boolean isPci;
    private boolean reconnectionAllowed;
    private String deploymentCluster;
    private CallDirection callDirection;
    private String message;
    private long startTime;
    private boolean previewCall;
    private String experienceId;
}
