package com.observeai.platform.realtime.neutrino.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mongodb")
@Getter
@Setter
public class MongoProperties {
    private String uri;
}
