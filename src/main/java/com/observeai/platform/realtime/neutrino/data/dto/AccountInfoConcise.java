package com.observeai.platform.realtime.neutrino.data.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccountInfoConcise implements Serializable {
    private String name;
    private String observeAccountId;
    private String integrationType;
    private boolean enableHtv;
    private boolean callNotesEnabled;
    private boolean reconnectionAllowed;
    private boolean monitoringEnabled;
    private KwsParams kwsParams;
    private SilenceProperties silenceProperties;
    private ConversationDurationProperties conversationDurationProperties;
    private PreviewCallsTranscriptionConfigs previewCallsTranscriptionConfigs;
    private SupervisorAssistProperties supervisorAssistProperties;
    private MetadataBasedProperties metadataBasedProperties;
    private String vendor;
    private String deploymentCluster;
}
