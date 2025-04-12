package com.observeai.platform.realtime.neutrino.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pauth")
@Getter
@Setter
public class PartnerAuthProperties {
    String baseUrl;
    String billBaseUrl;
    String tokenPath;

    String tokenUriTemplate(String cluster) {
        return ("BILL".equals(cluster) ? billBaseUrl : baseUrl) + tokenPath;
    }
}
