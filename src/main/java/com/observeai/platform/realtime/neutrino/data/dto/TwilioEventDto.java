package com.observeai.platform.realtime.neutrino.data.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@ToString
public class TwilioEventDto {
    private String taskPriority;
    private String eventType;
    private String workflowName;
    private Integer workerTimeInPreviousActivityMs;
    private Long timestamp;
    private Integer taskAge;
    private String reason;
    private String workerPreviousActivitySid;
    private Integer workerTimeInPreviousActivity;
    private String taskAssignmentStatus;
    private String taskAttributes;
    private String workerSid;
    private String taskChannelUniqueName;
    private String workspaceName;
    private String taskChannelSid;
    private Long taskQueueEnteredDate;
    private Long taskDateCreated;
    private String workerActivityName;
    private String reservationSid;
    private String resourceType;
    private String taskQueueName;
    private String workerActivitySid;
    private String workflowSid;
    private String accountSid;
    private String workerName;
    private String sid;
    private Long timestampMs;
    private String taskQueueTargetExpression;
    private String workspaceSid;
    private String taskQueueSid;
    private String workerPreviousActivityName;
    private String eventDescription;
    private String taskSid;
    private String resourceSid;
    private String workerAttributes;

    // Observe details
    private String observeAccountId;
    private String observeUserId;
    private CallDirection direction;
}
