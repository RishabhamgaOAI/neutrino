package com.observeai.platform.realtime.neutrino.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "chitragupta")
public class ChitraGuptaProperties {

    private ServiceProperties service;
    private ClientProperties client;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ServiceProperties {
        private String baseUrl;
        private String appSecret;
        private String billBaseUrl;
        private String billAppSecret;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ClientProperties {
        private MonitorEventProperties monitorEvent;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class MonitorEventProperties {
            private int threadPoolSize;
        }
    }
}