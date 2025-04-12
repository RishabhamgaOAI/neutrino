package com.observeai.platform.realtime.commons.data.messages.notifications;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.observeai.platform.realtime.neutrino.client.NotificationProperties;
import com.observeai.platform.realtime.neutrino.data.CallAttachedMetadata;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CallTopicStartNotification extends CallTopicEventNotification {
    protected NotificationMessage.CallStartNotificationMessage message;

    public CallTopicStartNotification(CallEventDto callEventDto, NotificationProperties notificationProperties, CallAttachedMetadata callAttachedMetadata) {
        super(callEventDto, notificationProperties);
        this.message = NotificationMessage.CallStartNotificationMessage.builder()
                .text(callEventDto.getMessage())
                .time(callEventDto.getTime())
                .type(callEventDto.getType().toString())
                .url(notificationProperties.getCesWsUrlPath() + "/" + callEventDto.getObserveCallId())
                .direction(callEventDto.getCallDirection())
                .attachedVariables(callAttachedMetadata)
                .vendorCallId(callEventDto.getVendorCallId())
                .startTime(callEventDto.getStartTime())
                .previewCall(callEventDto.isPreviewCall())
                .experienceId(callEventDto.getExperienceId())
                .build();
    }
}
