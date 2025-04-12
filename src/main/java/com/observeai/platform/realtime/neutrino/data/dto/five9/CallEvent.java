package com.observeai.platform.realtime.neutrino.data.dto.five9;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CallEvent {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AgentDetails {
        private String stationType;
        private String firstAgent;
        private String fullName;
        private String id;
        private String userName;
        private String stationId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CallDetails {
        private String sessionId;
        private String typeName;
        private String callId;
        private String domainId;
        private String number;
        private String domainName;
    }

    @JsonUnwrapped(prefix = "Agent.")
    public AgentDetails agentDetails;
    @JsonUnwrapped(prefix = "Call.")
    public CallDetails callDetails;
}
