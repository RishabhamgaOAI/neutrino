package com.observeai.platform.realtime.neutrino.exception.handler;

import com.observeai.platform.realtime.neutrino.exception.InternalAppErrorResponse;
import com.observeai.platform.realtime.neutrino.exception.dravity.DravityExceptions;
import com.observeai.platform.realtime.neutrino.exception.neutrino.NeutrinoExceptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.observeai.platform.realtime.neutrino.exception.ErrorConstants.INTERNAL_SERVER_ERROR;
import static java.net.HttpURLConnection.*;

@RestControllerAdvice
@Slf4j
public class DravityExceptionHandler extends ExtAppExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InternalAppErrorResponse handleResourceNotFoundException(DravityExceptions.ResourceNotFoundException e) {
        return getErrorDetailInstance(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InternalAppErrorResponse handleInternalServerErrorException(DravityExceptions.InternalServerErrorException e) {
        return getErrorDetailInstance(e);
    }

    public void handleException(int rawStatusCode, String errorBody) {
        log.error("Dravity exception: {} with error body: {}", rawStatusCode, errorBody);
        InternalAppErrorResponse errorResponse = mapErrorBody(rawStatusCode, errorBody, InternalAppErrorResponse.class);

        switch (rawStatusCode) {
            case HTTP_NOT_FOUND:
                throw new DravityExceptions.ResourceNotFoundException(errorResponse);
            case HTTP_INTERNAL_ERROR:
                throw new DravityExceptions.InternalServerErrorException(errorResponse);
            case HTTP_BAD_REQUEST:
                throw new DravityExceptions.BadRequestException(errorResponse);
            default:
                log.error("Encountered unhandled dravity exception: {}", errorBody);
                throw new NeutrinoExceptions.UnhandledException(errorBody);
        }
    }

    private InternalAppErrorResponse getErrorDetailInstance(DravityExceptions.BaseException e) {
        return new InternalAppErrorResponse(INTERNAL_SERVER_ERROR, e.toString());
    }
}
