package com.observeai.platform.realtime.commons.data.messages.notifications;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.observeai.platform.realtime.neutrino.data.CallAttachedMetadata;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NotificationMessage {
    private String text;
    private long time;
    private String type;
    private String url;
    private boolean previewCall;
    private String experienceId;

    @EqualsAndHashCode(callSuper = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder(toBuilder = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CallStartNotificationMessage extends NotificationMessage {
        private CallDirection direction;
        private Long startTime;
        private String vendorCallId;
        private CallAttachedMetadata attachedVariables;

        public CallStartNotificationMessage(String text, long time, String type, String url,
            CallDirection direction, Long startTime, String vendorCallId, CallAttachedMetadata attachedVariables, boolean previewCall, String experienceId) {
            super(text, time, type, url, previewCall, experienceId);
            this.direction = direction;
            this.startTime = startTime;
            this.vendorCallId = vendorCallId;
            this.attachedVariables = attachedVariables;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    @SuperBuilder(toBuilder = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CallEndNotificationMessage extends NotificationMessage {
        private CallDirection direction;
        private Long startTime;
        private Long endTime;
        private String vendorCallId;
        private String partnerMeetingId;
        private String vendor;

        public CallEndNotificationMessage(String text, long time, String type, String url,
            CallDirection direction, Long startTime, Long endTime, String vendorCallId, String partnerMeetingId, boolean previewCall, String experienceId, String vendor) {
            super(text, time, type, url, previewCall, experienceId);
            this.direction = direction;
            this.startTime = startTime;
            this.endTime = endTime;
            this.vendorCallId = vendorCallId;
            this.partnerMeetingId = partnerMeetingId;
            this.vendor= vendor;
        }
    }
}
