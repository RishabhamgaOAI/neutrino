package com.observeai.platform.realtime.neutrino.client.five9;

import com.observeai.platform.realtime.neutrino.NeutrinoBaseTest;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionResponse;
import com.observeai.platform.realtime.neutrino.exception.five9.Five9Exceptions;
import com.observeai.platform.realtime.neutrino.util.five9.Five9Properties;
import com.observeai.platform.realtime.neutrino.util.five9.Five9Util;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import com.observeai.platform.realtime.neutrino.util.http.RestTemplateWrapper;
import com.observeai.platform.realtime.neutrino.utils.Five9TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static com.observeai.platform.realtime.neutrino.utils.Five9TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = Five9Properties.class)
@ActiveProfiles("test")
class SubscriptionClientTest extends NeutrinoBaseTest {

    @Autowired
    Five9Properties five9Properties;
    @Spy
    @InjectMocks
    Five9Util five9Util;
    @Mock
    RestTemplateWrapper restTemplateWrapper;

    SubscriptionClient subscriptionClient;


    @BeforeEach
    void init() {
        subscriptionClient = new SubscriptionClient(five9Properties, restTemplateWrapper, five9Util);
        doReturn(sampleHttpHeaderWithToken()).when(five9Util).getHttpHeaderWithToken(SAMPLE_DOMAIN_ID);
    }

    @Test
    void getSubscriptionById_ValidResponse_ShouldReturnSubscriptionId() {
        String url = String.format("%s/domains/%s/subscriptions/%s", getFive9BaseUrl(), SAMPLE_DOMAIN_ID, SAMPLE_SUBSCRIPTION_ID);
        HttpHeaders headers = Five9TestUtil.sampleHttpHeaderWithToken();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> httpEntity = new HttpEntity<>(null, headers);
        HttpResponse<SubscriptionResponse> httpResponse = Five9TestUtil.buildHttpResponse(Five9TestUtil.sampleSubscriptionResponse(), HttpStatus.OK);
        when(restTemplateWrapper.exchangeRequest(eq(url), eq(HttpMethod.GET), eq(httpEntity), eq(SubscriptionResponse.class), any())).thenReturn(httpResponse);

        SubscriptionResponse response = subscriptionClient.getSubscriptionById(Five9TestUtil.SAMPLE_DOMAIN_ID, Five9TestUtil.SAMPLE_SUBSCRIPTION_ID);
        assertThat(response.getSubscriptionId()).isEqualTo(Five9TestUtil.SAMPLE_SUBSCRIPTION_ID);
    }

    @Test
    void getSubscriptionById_4xxResponse_ShouldThrowException() {
        String url = String.format("%s/domains/%s/subscriptions/%s", getFive9BaseUrl(), SAMPLE_DOMAIN_ID, SAMPLE_SUBSCRIPTION_ID);
        HttpHeaders headers = Five9TestUtil.sampleHttpHeaderWithToken();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> httpEntity = new HttpEntity<>(null, headers);
        HttpResponse<SubscriptionResponse> httpResponse = Five9TestUtil.buildHttpResponse(Five9TestUtil.sampleSubscriptionResponse(), HttpStatus.OK);
        when(restTemplateWrapper.getHttpResponse(any(), any(), any(), any())).thenThrow(sample4xxResponse());
        when(restTemplateWrapper.exchangeRequest(any(), any(), any(), any(), any())).thenCallRealMethod();

        assertThatThrownBy(() -> subscriptionClient.getSubscriptionById(Five9TestUtil.SAMPLE_DOMAIN_ID, Five9TestUtil.SAMPLE_SUBSCRIPTION_ID)).isInstanceOf(Five9Exceptions.BadRequestException.class);
    }

    @Test
    void getSubscriptionById_5xxResponse_ShouldThrowException() {
        String url = String.format("%s/domains/%s/subscriptions/%s", getFive9BaseUrl(), SAMPLE_DOMAIN_ID, SAMPLE_SUBSCRIPTION_ID);
        HttpHeaders headers = Five9TestUtil.sampleHttpHeaderWithToken();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> httpEntity = new HttpEntity<>(null, headers);
        HttpResponse<SubscriptionResponse> httpResponse = Five9TestUtil.buildHttpResponse(Five9TestUtil.sampleSubscriptionResponse(), HttpStatus.OK);
        when(restTemplateWrapper.getHttpResponse(any(), any(), any(), any())).thenThrow(sample5xxResponse());
        when(restTemplateWrapper.exchangeRequest(any(), any(), any(), any(), any())).thenCallRealMethod();

        assertThatThrownBy(() -> subscriptionClient.getSubscriptionById(Five9TestUtil.SAMPLE_DOMAIN_ID, Five9TestUtil.SAMPLE_SUBSCRIPTION_ID)).isInstanceOf(Five9Exceptions.InternalServerErrorException.class);
    }
}
