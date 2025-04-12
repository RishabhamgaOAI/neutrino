package com.observeai.platform.realtime.neutrino.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "call-metrics")
@Getter
@Setter
public class CallMetricsConfig {
    private boolean enabled;
    private long waitForStreamInSeconds;
    private int schedulerThreadCount;
}
