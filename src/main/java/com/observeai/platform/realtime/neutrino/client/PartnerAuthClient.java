package com.observeai.platform.realtime.neutrino.client;

import com.observeai.platform.realtime.neutrino.data.dto.pauth.TokenResponse;
import com.observeai.platform.realtime.neutrino.exception.handler.ExceptionHandlerFactory;
import com.observeai.platform.realtime.neutrino.exception.handler.ExtAppExceptionHandler;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import com.observeai.platform.realtime.neutrino.util.http.RestTemplateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PartnerAuthClient {

    private final PartnerAuthProperties partnerAuthProperties;
    private final RestTemplateWrapper restTemplateWrapper;
    private final ExtAppExceptionHandler exceptionHandler = ExceptionHandlerFactory.getPauthExceptionHandler();

    public TokenResponse getToken(String cluster, String partnerUid) {
        log.info("fetching token for partnerUid: {}, cluster: {}", partnerUid, cluster);
        String url = UriComponentsBuilder.fromHttpUrl(partnerAuthProperties.tokenUriTemplate(cluster)).build(partnerUid).toString();
        HttpEntity<Void> httpEntity = new HttpEntity<>(null, null);
        HttpResponse<TokenResponse> httpResponse = restTemplateWrapper.exchangeRequest(url, HttpMethod.GET, httpEntity, TokenResponse.class, exceptionHandler);
        return httpResponse.getResponse();
    }
}
