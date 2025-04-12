package com.observeai.platform.realtime.neutrino.client.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "http")
public class HttpProperties {
    private int maxTotalConnections;
    private int maxConnectionsPerRoute;
    private int inactivityValidationIntervalInMs;
    private int socketTimeOutInMillis;
    private int connectionTimeoutInMillis;
    private int connectionRequestTimeout;
    private int idleConnectionEvictionTimeout;
    private boolean expectContinueEnabled;
}
