package com.observeai.platform.realtime.neutrino.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {
    private String baseUrl;
    private String authHost;
    private String restrictedAuthHost;
    private String appSecret;
    private String appId;
    private String assetAlias;
    private String verifyPath;
}
