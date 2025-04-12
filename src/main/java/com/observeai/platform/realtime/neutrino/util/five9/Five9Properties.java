package com.observeai.platform.realtime.neutrino.util.five9;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "five9")
@Getter
@Setter
public class Five9Properties {
    private String baseUrl;
    private String oauthBaseUrl;
    private String subscriptionByIdPath = "/domains/{domainId}/subscriptions/{subscriptionId}";
    private String directivesPath = "/domains/{domainId}/directives";
    private String attachDirectivePath = "/domains/{domainId}/subscriptions/{subscriptionId}/directive/{directiveId}";
    private String introspectPath = "/v1/introspect?client_id={client_id}";

    // directive related properties
    private String trustToken;
    private String callEventUrl;
    private String voiceStreamEventUrl;
    private String grpcTargetUrl;
    private String sipTargetUri;


    public String subscriptionByIdUriTemplate() {
        return baseUrl + subscriptionByIdPath;
    }

    public String directivesUriTemplate() {
        return baseUrl + directivesPath;
    }

    public String attachDirectiveUriTemplate() {
        return baseUrl + attachDirectivePath;
    }

    public String introspectUrl() {
        return oauthBaseUrl + introspectPath;
    }
}
