package com.observeai.platform.realtime.neutrino.client.five9;

import com.observeai.platform.realtime.neutrino.NeutrinoBaseTest;
import com.observeai.platform.realtime.neutrino.data.dto.five9.directive.DirectiveRequest;
import com.observeai.platform.realtime.neutrino.data.dto.five9.directive.DirectiveResponse;
import com.observeai.platform.realtime.neutrino.exception.five9.Five9Exceptions;
import com.observeai.platform.realtime.neutrino.util.five9.Five9Properties;
import com.observeai.platform.realtime.neutrino.util.five9.Five9Util;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import com.observeai.platform.realtime.neutrino.util.http.RestTemplateWrapper;
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
class DirectiveClientTest extends NeutrinoBaseTest {

    @Autowired
    Five9Properties five9Properties;
    @Spy @InjectMocks
    Five9Util five9Util;
    @Mock
    RestTemplateWrapper restTemplateWrapper;

    DirectiveClient directiveClient;

    @BeforeEach
    void init() {
        directiveClient = new DirectiveClient(five9Properties, restTemplateWrapper, five9Util);
        doReturn(sampleHttpHeaderWithToken()).when(five9Util).getHttpHeaderWithToken(SAMPLE_DOMAIN_ID);
    }

    @Test
    void createDirective_ValidResponse_ShouldCreateDirective() {
        String url = String.format("%s/domains/%s/directives", getFive9BaseUrl(), SAMPLE_DOMAIN_ID);
        HttpHeaders headers = sampleHttpHeaderWithToken();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DirectiveRequest> httpEntity = new HttpEntity<>(sampleDirectiveRequest(), headers);
        HttpResponse<DirectiveResponse> httpResponse = buildHttpResponse(sampleDirectiveResponse(), HttpStatus.CREATED);
        when(restTemplateWrapper.exchangeRequest(eq(url), eq(HttpMethod.POST), eq(httpEntity), eq(DirectiveResponse.class), any())).thenReturn(httpResponse);
        DirectiveResponse response = directiveClient.createDirective(SAMPLE_DOMAIN_ID, sampleDirectiveRequest());

        assertThat(response.getDirectiveId()).isEqualTo("directive1");
    }

    @Test
    void createDirective_4xxResponse_ShouldThrowException() {
        String url = String.format("%s/domains/%s/directives", getFive9BaseUrl(), SAMPLE_DOMAIN_ID);
        HttpHeaders headers = sampleHttpHeaderWithToken();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DirectiveRequest> httpEntity = new HttpEntity<>(sampleDirectiveRequest(), headers);
        HttpResponse<DirectiveResponse> httpResponse = buildHttpResponse(sampleDirectiveResponse(), HttpStatus.CREATED);
        when(restTemplateWrapper.getHttpResponse(any(), any(), any(), any())).thenThrow(sample4xxResponse());
        when(restTemplateWrapper.exchangeRequest(any(), any(), any(), any(), any())).thenCallRealMethod();

        assertThatThrownBy(() -> directiveClient.createDirective(SAMPLE_DOMAIN_ID, sampleDirectiveRequest())).isInstanceOf(Five9Exceptions.BadRequestException.class);
    }

    @Test
    void createDirective_5xxResponse_ShouldThrowException() {
        String url = String.format("%s/domains/%s/directives", getFive9BaseUrl(), SAMPLE_DOMAIN_ID);
        HttpHeaders headers = sampleHttpHeaderWithToken();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DirectiveRequest> httpEntity = new HttpEntity<>(sampleDirectiveRequest(), headers);
        HttpResponse<DirectiveResponse> httpResponse = buildHttpResponse(sampleDirectiveResponse(), HttpStatus.CREATED);
        when(restTemplateWrapper.getHttpResponse(any(), any(), any(), any())).thenThrow(sample5xxResponse());
        when(restTemplateWrapper.exchangeRequest(any(), any(), any(), any(), any())).thenCallRealMethod();

        assertThatThrownBy(() -> directiveClient.createDirective(SAMPLE_DOMAIN_ID, sampleDirectiveRequest())).isInstanceOf(Five9Exceptions.InternalServerErrorException.class);

    }

    @Test
    void attachDirective_ValidResponse_ShouldAttachDirective() {
        String url = String.format("%s/domains/%s/subscriptions/%s/directive/%s", getFive9BaseUrl(), SAMPLE_DOMAIN_ID, SAMPLE_SUBSCRIPTION_ID, SAMPLE_DIRECTIVE_ID);
        HttpHeaders headers = sampleHttpHeaderWithToken();
        HttpEntity<Void> httpEntity = new HttpEntity<>(null, headers);
        HttpResponse<Void> httpResponse = buildHttpResponse(null, HttpStatus.NO_CONTENT);
        when(restTemplateWrapper.exchangeRequest(eq(url), eq(HttpMethod.PUT), eq(httpEntity), eq(Void.class), any())).thenReturn(httpResponse);
        directiveClient.attachDirectiveToSubscription(SAMPLE_DOMAIN_ID, SAMPLE_DIRECTIVE_ID, SAMPLE_SUBSCRIPTION_ID);
    }

    @Test
    void attachDirective_4xxResponse_ShouldThrowException() {
        String url = String.format("%s/domains/%s/subscriptions/%s/directive/%s", getFive9BaseUrl(), SAMPLE_DOMAIN_ID, SAMPLE_SUBSCRIPTION_ID, SAMPLE_DIRECTIVE_ID);
        HttpHeaders headers = sampleHttpHeaderWithToken();
        HttpEntity<Void> httpEntity = new HttpEntity<>(null, headers);
        HttpResponse<Void> httpResponse = buildHttpResponse(null, HttpStatus.NO_CONTENT);
        when(restTemplateWrapper.getHttpResponse(any(), any(), any(), any())).thenThrow(sample4xxResponse());
        when(restTemplateWrapper.exchangeRequest(any(), any(), any(), any(), any())).thenCallRealMethod();

        assertThatThrownBy(() -> directiveClient.attachDirectiveToSubscription(SAMPLE_DOMAIN_ID, SAMPLE_DIRECTIVE_ID, SAMPLE_SUBSCRIPTION_ID)).isInstanceOf(Five9Exceptions.BadRequestException.class);
    }

    @Test
    void attachDirective_5xxResponse_ShouldThrowException() {
        String url = String.format("%s/domains/%s/subscriptions/%s/directive/%s", getFive9BaseUrl(), SAMPLE_DOMAIN_ID, SAMPLE_SUBSCRIPTION_ID, SAMPLE_DIRECTIVE_ID);
        HttpHeaders headers = sampleHttpHeaderWithToken();
        HttpEntity<Void> httpEntity = new HttpEntity<>(null, headers);
        HttpResponse<Void> httpResponse = buildHttpResponse(null, HttpStatus.NO_CONTENT);
        when(restTemplateWrapper.getHttpResponse(any(), any(), any(), any())).thenThrow(sample5xxResponse());
        when(restTemplateWrapper.exchangeRequest(any(), any(), any(), any(), any())).thenCallRealMethod();

        assertThatThrownBy(() -> directiveClient.attachDirectiveToSubscription(SAMPLE_DOMAIN_ID, SAMPLE_DIRECTIVE_ID, SAMPLE_SUBSCRIPTION_ID)).isInstanceOf(Five9Exceptions.InternalServerErrorException.class);

    }
}
