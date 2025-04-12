package com.observeai.platform.realtime.neutrino.util;

import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;

public class CallEventUtil {

    public static CallEventDto buildCallEventDto(Call call, CallEventType type) {
        CallStartMessage startMessage = call.getStartMessage();
        String message = type.getMessage();

        return new CallEventDto(System.currentTimeMillis(), type, startMessage.getVendorCallId(),
                startMessage.getPartnerMeetingId(), call.getObserveCallId(), call.getMasterCallId(), call.getParentCallId(),
                startMessage.getVendor(), startMessage.getAccountId(), startMessage.getAgentId(), startMessage.isRecordAudio(), startMessage.isSupervisorAssistAudioEnabled(),
                startMessage.isPci(), startMessage.isReconnectionAllowed(), startMessage.getDeploymentCluster(), startMessage.getDirection(), message, call.getStartTime(),
                call.getStartMessage().isPreviewCall(), call.getStartMessage().getExperienceId());
    }
}
