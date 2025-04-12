package com.observeai.platform.realtime.neutrino.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "dravity")
@Getter
@Setter
public class DravityProperties {
    private String url;
    private String accountAndUserInfoByVendorUserIdPath;
    private String accountInfoByVendorAccountIdPath;
    private String accountInfoByObserveAccountIdPath;
    private String configsForCallByObserveIdsPath;

    public String accountAndUserInfoByVendorUserIdUrl() {
        return url + accountAndUserInfoByVendorUserIdPath;
    }

    public String accountInfoByVendorAccountIdUrl() {
        return url + accountInfoByVendorAccountIdPath;
    }

    public String accountInfoByObserveAccountIdUrl() {
        return url + accountInfoByObserveAccountIdPath;
    }

    public String configsForCallByObserveIdsUrl() {
        return url + configsForCallByObserveIdsPath;
    }
}
