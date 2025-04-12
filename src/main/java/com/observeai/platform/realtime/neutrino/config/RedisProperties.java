package com.observeai.platform.realtime.neutrino.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "redis")
@Getter
@Setter
public class RedisProperties {
    private String host;
    private int port;
}
