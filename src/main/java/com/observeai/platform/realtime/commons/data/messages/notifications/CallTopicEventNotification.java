package com.observeai.platform.realtime.commons.data.messages.notifications;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.observeai.platform.realtime.commons.data.enums.CallTopicMessageType;
import com.observeai.platform.realtime.commons.data.messages.CallTopicMessage;
import com.observeai.platform.realtime.neutrino.client.NotificationProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CallTopicEventNotification extends CallTopicMessage {
    private String notificationId;
    private String serviceId;
    private NotificationChannel channel;
    protected NotificationMessage message;

    public CallTopicEventNotification(String callId, String masterCallId, String parentCallId, CallTopicMessageType messageType,
                                      String accountId, String userId, String serviceId, NotificationChannel channel,
                                      NotificationMessage message) {
        super(callId, masterCallId, parentCallId, accountId, userId, messageType);
        this.notificationId = UUID.randomUUID().toString();
        this.serviceId = serviceId;
        this.channel = channel;
        this.message = message;
    }

    public CallTopicEventNotification(CallEventDto callEventDto, NotificationProperties notificationProperties) {
        this(callEventDto.getObserveCallId(), callEventDto.getMasterCallId(), callEventDto.getParentCallId(),
                callEventDto.getType().getCallTopicMessageType(), callEventDto.getObserveAccountId(),
                callEventDto.getObserveUserId(), notificationProperties.getServiceId(), NotificationChannel.APP,
                NotificationMessage.builder()
                        .text(callEventDto.getMessage())
                        .time(callEventDto.getTime())
                        .type(callEventDto.getType().toString())
                        .url(notificationProperties.getCesWsUrlPath() + "/" + callEventDto.getObserveCallId())
                        .previewCall(callEventDto.isPreviewCall())
                        .experienceId(callEventDto.getExperienceId())
                        .build());
    }

    protected static String getCesCallUrl(NotificationProperties properties, String callId) {
        return properties.getCesWsUrlPath() + "/" + callId;
    }
}
