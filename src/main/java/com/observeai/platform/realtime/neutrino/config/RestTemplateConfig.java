package com.observeai.platform.realtime.neutrino.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RestTemplateConfig {

    /*@Bean
    @ConfigurationProperties(prefix = "keystore")
    public KeyStoreProperties keyStoreProperties() {
        return new KeyStoreProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "http")
    public HttpProperties httpProperties() {
        return new HttpProperties();
    }

    @Bean
    public KeyStoreFactory keyStoreFactory() {
        return new KeyStoreFactory(keyStoreProperties());
    }

    @Bean
    public HttpClientFactory httpClientFactory() {
        return new HttpClientFactory(httpProperties(), keyStoreFactory());
    }*/

    /*@Bean
    public RestTemplate restTemplate() {
        final HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientFactory().getHttpClient());
        final RestTemplate restTemplate = new RestTemplate(httpComponentsClientHttpRequestFactory);
        final List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();

        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
                jsonConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON,
                        MediaType.parseMediaType("application/hal+json"), MediaType.TEXT_PLAIN,
                        MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_HTML));
                jsonConverter.setObjectMapper(ObjectMapperFactory.getPascalCaseObjectMapper());
            }
        }
        return restTemplate;
    }*/
}
