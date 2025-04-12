package com.observeai.platform.realtime.neutrino.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "deepgram")
@Getter
@Setter
public class DeepgramProperties {
    private String host;
    private String betaHost;
    private String secretKey;

    private String onPremHost;
    private String onPremSecretKey;

    private int port;
    private String wsScheme;
    private String realtimePath;
    private String modelName;
    private boolean interimResults;
    private long cutoffToAcceptNonFinalInMillis;
}
