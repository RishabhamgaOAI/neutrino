package com.observeai.platform.realtime.neutrino.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.exception.neutrino.NeutrinoExceptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
    abstract exception handler class for external services
 */
@RestControllerAdvice
@Slf4j
public abstract class ExtAppExceptionHandler {

    public abstract void handleException(int rawStatusCode, String errorBody);

    protected <T> T mapErrorBody(int rawStatusCode, String errorBody, Class<T> type) {
        try {
            return new ObjectMapper().readValue(errorBody, type);
        } catch (JsonProcessingException ex) {
            log.error("Unable to parse {} body to custom error class {}. detailed error {}", errorBody, type.getName(), ex.getMessage());
            throw new NeutrinoExceptions.JsonParseException(ex.getMessage());
        }
    }
}
