package com.observeai.platform.realtime.neutrino.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "call")
@Getter
@Setter
public class CallProperties {
    private Long timeoutForActiveProcessingInSeconds;
    private Long timeoutForMonitoringInSeconds;
    private Long maxCallDurationInSeconds;
    private Long timeoutForReconnectionInSeconds;
    private List<Integer> eligibleReconnectionErrorCodes;
}
