package com.observeai.platform.realtime.neutrino.client.five9;

import com.observeai.platform.realtime.neutrino.data.dto.five9.directive.DirectiveRequest;
import com.observeai.platform.realtime.neutrino.data.dto.five9.directive.DirectiveResponse;
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
public class DirectiveClient {

    private final Five9Properties five9Properties;
    private final RestTemplateWrapper restTemplateWrapper;
    private final ExtAppExceptionHandler exceptionHandler = ExceptionHandlerFactory.getFive9ExceptionHandler();
    private final Five9Util five9Util;

    public DirectiveResponse createDirective(String domainId, DirectiveRequest request) {
        String url = UriComponentsBuilder.fromHttpUrl(five9Properties.directivesUriTemplate()).build(domainId).toString();
        final HttpHeaders headers = five9Util.getHttpHeaderWithToken(domainId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DirectiveRequest> httpEntity = new HttpEntity<>(request, headers);
        HttpResponse<DirectiveResponse> response = restTemplateWrapper.exchangeRequest(url, HttpMethod.POST, httpEntity, DirectiveResponse.class, exceptionHandler);
        return response.getResponse();
    }

    public void attachDirectiveToSubscription(String domainId, String directiveId, String subscriptionId) {
        String url = UriComponentsBuilder.fromHttpUrl(five9Properties.attachDirectiveUriTemplate()).build(domainId, subscriptionId, directiveId).toString();
        final HttpHeaders headers = five9Util.getHttpHeaderWithToken(domainId);
        HttpEntity<Void> httpEntity = new HttpEntity<>(null, headers);
        HttpResponse<Void> response = restTemplateWrapper.exchangeRequest(url, HttpMethod.PUT, httpEntity, Void.class, exceptionHandler);
    }
}
