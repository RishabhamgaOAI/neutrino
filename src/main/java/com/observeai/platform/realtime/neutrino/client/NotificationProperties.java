package com.observeai.platform.realtime.neutrino.client;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "notification")
@Getter
@Setter
public class NotificationProperties {
    private String host;
    private String notifyPath;
    private String serviceId;
    private String alertsType;
    private String actionUrl;
    private String cesWsUrlPath;
    private int maxRetryAttempts;
    private long backoffDelay;
    private long backoffMaxDelay;
    private long backoffMultiplier;
}
