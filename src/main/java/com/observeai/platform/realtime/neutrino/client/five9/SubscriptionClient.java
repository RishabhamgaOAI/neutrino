package com.observeai.platform.realtime.neutrino.client.five9;


import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionResponse;
import com.observeai.platform.realtime.neutrino.exception.handler.ExceptionHandlerFactory;
import com.observeai.platform.realtime.neutrino.exception.handler.ExtAppExceptionHandler;
import com.observeai.platform.realtime.neutrino.util.five9.Five9Properties;
import com.observeai.platform.realtime.neutrino.util.five9.Five9Util;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import com.observeai.platform.realtime.neutrino.util.http.RestTemplateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;


@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SubscriptionClient {

    private final Five9Properties five9Properties;
    private final RestTemplateWrapper restTemplateWrapper;
    private final ExtAppExceptionHandler exceptionHandler = ExceptionHandlerFactory.getFive9ExceptionHandler();
    private final Five9Util five9Util;

    public SubscriptionResponse getSubscriptionById(String domainId, String subscriptionId) {
        String url = UriComponentsBuilder.fromHttpUrl(five9Properties.subscriptionByIdUriTemplate()).build(domainId, subscriptionId).toString();
        final HttpHeaders headers = five9Util.getHttpHeaderWithToken(domainId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);
        HttpResponse<SubscriptionResponse> response = restTemplateWrapper.exchangeRequest(url, HttpMethod.GET, httpEntity, SubscriptionResponse.class, exceptionHandler);
        return response.getResponse();
    }
}
