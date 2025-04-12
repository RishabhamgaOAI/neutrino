package com.observeai.platform.realtime.neutrino.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "deepgram.pool")
public class DeepgramPoolProperties {
    Map<String, IntegrationPoolConfigs> properties;
}