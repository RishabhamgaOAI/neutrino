package com.observeai.platform.realtime.commons.data.messages.notifications;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.observeai.platform.realtime.commons.data.messages.notifications.NotificationMessage.CallEndNotificationMessage;
import com.observeai.platform.realtime.neutrino.client.NotificationProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CallTopicEndNotification extends CallTopicEventNotification {
    private NotificationMessage.CallEndNotificationMessage message;

    public CallTopicEndNotification(CallEventDto callEventDto, NotificationProperties properties) {
        super(callEventDto, properties);
        this.message = new CallEndNotificationMessage(callEventDto.getMessage(),
            callEventDto.getTime(), callEventDto.getType().toString(),
            getCesCallUrl(properties, callEventDto.getObserveCallId()),
            callEventDto.getCallDirection(), callEventDto.getStartTime(),
            callEventDto.getTime(), callEventDto.getVendorCallId(),
                callEventDto.getPartnerMeetingId(),
                callEventDto.isPreviewCall(),
                callEventDto.getExperienceId(), callEventDto.getVendor());
    }
}
