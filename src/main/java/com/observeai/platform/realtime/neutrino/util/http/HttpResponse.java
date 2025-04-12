package com.observeai.platform.realtime.neutrino.util.http;

import org.springframework.http.HttpHeaders;

public class HttpResponse<T> {
    private final T responseEntity;
    private final HttpHeaders httpHeaders;
    private final Error error;

    public HttpResponse(T responseEntity, HttpHeaders httpHeaders, Error error) {
        this.responseEntity = responseEntity;
        this.httpHeaders = httpHeaders;
        this.error = error;
    }

    public static <T> HttpResponse<T> buildHttpResponseEntity(T responseEntity, HttpHeaders headers) {
        return new HttpResponse<>(responseEntity, headers, null);
    }

    public static <T> HttpResponse<T> buildError(Error error) {
        return new HttpResponse<>(null, null, error);
    }

    public boolean hasError() {
        return this.error != null;
    }

    public Error getError() {
        return this.error;
    }

    public T getResponse() {
        return this.responseEntity;
    }

    public HttpHeaders getHeaders() {
        return this.httpHeaders;
    }
}
