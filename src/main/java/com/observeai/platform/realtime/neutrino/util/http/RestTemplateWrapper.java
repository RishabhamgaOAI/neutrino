package com.observeai.platform.realtime.neutrino.util.http;

import com.observeai.platform.realtime.neutrino.exception.handler.ExtAppExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class RestTemplateWrapper {
    private final RestTemplate restTemplate;

    public <T> HttpResponse<T>  exchangeRequest(String url, HttpMethod httpMethod, HttpEntity entity, Class<T> responseType) {
        Error error;
        try {
            return getHttpResponse(url, httpMethod, entity, responseType);
        } catch (HttpClientErrorException ex) {
            error = new Error("client_error", ex);
        } catch (HttpServerErrorException ex) {
            error = new Error("server_error", ex);
        } catch (RestClientException ex) {
            error = new Error("unknown_error", ex);
        }
        log.error("{}", error.getMessage(), error.getCause());
        return HttpResponse.buildError(error);
    }

    public <T> HttpResponse<T>  exchangeRequest(String url, HttpMethod httpMethod, HttpEntity entity, Class<T> responseType, ExtAppExceptionHandler exceptionHandler) {
        try {
            return getHttpResponse(url, httpMethod, entity, responseType);
        } catch (RestClientResponseException ex) {
            exceptionHandler.handleException(ex.getRawStatusCode(), ex.getResponseBodyAsString());
        }
        return null;
    }

    public <T> HttpResponse<T> getHttpResponse(String url, HttpMethod httpMethod, HttpEntity entity, Class<T> responseType) {
        final ResponseEntity<T> exchange = restTemplate.exchange(url, httpMethod, entity, responseType);
        return HttpResponse.buildHttpResponseEntity(exchange.getBody(), exchange.getHeaders());
    }
}
