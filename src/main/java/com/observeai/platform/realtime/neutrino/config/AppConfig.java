package com.observeai.platform.realtime.neutrino.config;

import com.observeai.platform.realtime.neutrino.client.http.HttpClientFactory;
import com.observeai.platform.realtime.neutrino.client.http.HttpProperties;
import com.observeai.platform.realtime.neutrino.util.ModelMapperFactory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AppConfig {
    private final HttpProperties httpProperties;


    @Bean
    public RestTemplate restTemplate() {
        HttpClientFactory httpClientFactory = new HttpClientFactory(httpProperties);
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClientFactory.getHttpClient()));
    }

    @Bean
    public ModelMapper modelMapper() {
        return ModelMapperFactory.getModelMapper();
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(2000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
