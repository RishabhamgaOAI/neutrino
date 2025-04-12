package com.observeai.platform.realtime.neutrino.data.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class AccountInfoWithVendorDetailsDto {
    private String id;
    private String name;
    private Integer parallelCalls;
    private Object parseConfig;
    private String integrationType;
    private boolean recordAudio;
    private boolean enableHtv;
    private boolean callNotesEnabled;
    private boolean isPci;
    private boolean reconnectionAllowed;
    private String primaryRegion;
    private String secondaryRegion;
    private String observeAccountId;
    private String deploymentCluster;
    private KwsParams kwsParams;
    private SilenceProperties silenceProperties;
    private CallSummaryProperties callSummaryProperties;
    private ConversationDurationProperties conversationDurationProperties;
    private SupervisorAssistProperties supervisorAssistProperties;
    private MetadataBasedProperties metadataBasedProperties;
    private VendorAccountDetailsDto vendorAccountDetails;

    @Getter
    @Setter
    @JsonNaming(SnakeCaseStrategy.class)
    public static class VendorAccountDetailsDto {
        private String vendor;
        private String vendorAccountId;
        private String partnerUid;
        private VendorAccountConfig config;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorAccountConfig {
        private GenesysIntegrationConfig genesysConfig;
        private NiceIntegrationConfig niceConfig;
        private String directiveId;
    }
}
