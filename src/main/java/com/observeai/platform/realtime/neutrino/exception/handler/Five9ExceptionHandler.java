package com.observeai.platform.realtime.neutrino.exception.handler;

import com.observeai.platform.realtime.neutrino.exception.InternalAppErrorResponse;
import com.observeai.platform.realtime.neutrino.exception.five9.Five9ErrorResponse;
import com.observeai.platform.realtime.neutrino.exception.five9.Five9Exceptions;
import com.observeai.platform.realtime.neutrino.exception.neutrino.NeutrinoExceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.observeai.platform.realtime.neutrino.exception.ErrorConstants.INTERNAL_SERVER_ERROR;
import static java.net.HttpURLConnection.*;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Five9ExceptionHandler extends ExtAppExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InternalAppErrorResponse handleBadRequestException(Five9Exceptions.BadRequestException e) {
        return getErrorDetailInstance(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InternalAppErrorResponse handleUnauthorizedException(Five9Exceptions.UnauthorizedException e) {
        return getErrorDetailInstance(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InternalAppErrorResponse handleInternalServerErrorException(Five9Exceptions.InternalServerErrorException e) {
        return getErrorDetailInstance(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InternalAppErrorResponse handleForbiddenErrorException(Five9Exceptions.ForbiddenException e) {
        return getErrorDetailInstance(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InternalAppErrorResponse handleNotFoundErrorException(Five9Exceptions.NotFoundException e) {
        return getErrorDetailInstance(e);
    }

    @Override
    public void handleException(int rawStatusCode, String errorBody) {
        log.error("Five9 exception: {} with error body: {}", rawStatusCode, errorBody);
        Five9ErrorResponse errorResponse = mapErrorBody(rawStatusCode, errorBody, Five9ErrorResponse.class);

        switch (rawStatusCode) {
            case HTTP_BAD_REQUEST:
                throw new Five9Exceptions.BadRequestException(errorResponse);
            case HTTP_UNAUTHORIZED:
                throw new Five9Exceptions.UnauthorizedException(errorResponse);
            case HTTP_FORBIDDEN:
                throw new Five9Exceptions.ForbiddenException(errorResponse);
            case HTTP_NOT_FOUND:
                throw new Five9Exceptions.NotFoundException(errorResponse);
            case HTTP_INTERNAL_ERROR:
                throw new Five9Exceptions.InternalServerErrorException(errorResponse);
            default:
                log.error("Encountered unhandled five9 exception with error body {}", errorBody);
                throw new NeutrinoExceptions.UnhandledException(errorBody);
        }
    }

    private InternalAppErrorResponse getErrorDetailInstance(Five9Exceptions.BaseException e) {
        return new InternalAppErrorResponse(INTERNAL_SERVER_ERROR, e.toString());
    }
}

