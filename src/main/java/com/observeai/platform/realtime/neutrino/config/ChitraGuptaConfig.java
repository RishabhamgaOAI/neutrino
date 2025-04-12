package com.observeai.platform.realtime.neutrino.config;

import com.observeai.platform.integration.commons.http.HttpClientFactory;
import com.observeai.platform.integration.commons.http.HttpProperties;
import com.observeai.platform.integration.commons.http.KeyStoreFactory;
import com.observeai.platform.integration.commons.http.KeyStoreProperties;
import com.observeai.platform.integration.commons.http.RestTemplateConfig;
import com.observeai.platform.integration.services.chitragupta.client.service.MonitoringServiceClientImpl;
import com.observeai.platform.integration.services.chitragupta.client.service.iface.MonitoringServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChitraGuptaConfig {
    private final ChitraGuptaProperties chitraGuptaProperties;

    @Bean
    @ConfigurationProperties(prefix = "chitragupta.client.http")
    public HttpProperties chitraguptaHttpProperties() {
        return new HttpProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "chitragupta.client.keystore")
    public KeyStoreProperties chitraguptaKeyStoreProperties() {
        return new KeyStoreProperties();
    }

    @Bean
    public MonitoringServiceClient monitoringServiceClient() {
        HttpClientFactory httpClientFactory =  new HttpClientFactory(chitraguptaHttpProperties(), new KeyStoreFactory(chitraguptaKeyStoreProperties()));
        RestTemplateConfig restTemplate = new RestTemplateConfig(httpClientFactory);
        return new MonitoringServiceClientImpl(chitraGuptaProperties.getClient().getMonitorEvent().getThreadPoolSize(), chitraGuptaProperties.getService().getBaseUrl(), chitraGuptaProperties.getService().getAppSecret(), restTemplate);
    }

    @Bean
    public MonitoringServiceClient restrictedMonitoringServiceClient() {
        HttpClientFactory httpClientFactory =  new HttpClientFactory(chitraguptaHttpProperties(), new KeyStoreFactory(chitraguptaKeyStoreProperties()));
        RestTemplateConfig restTemplate = new RestTemplateConfig(httpClientFactory);
        return new MonitoringServiceClientImpl(chitraGuptaProperties.getClient().getMonitorEvent().getThreadPoolSize(), chitraGuptaProperties.getService().getBillBaseUrl(), chitraGuptaProperties.getService().getBillAppSecret(), restTemplate);
    }

}
