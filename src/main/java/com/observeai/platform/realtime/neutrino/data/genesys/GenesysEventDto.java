package com.observeai.platform.realtime.neutrino.data.genesys;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
public class GenesysEventDto {
    private String callId;
    private String state;
    private String agentId;
    private String username;
}
