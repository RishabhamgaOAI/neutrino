package com.observeai.platform.realtime.neutrino.exception.handler;

import com.observeai.platform.realtime.neutrino.exception.InternalAppErrorResponse;
import com.observeai.platform.realtime.neutrino.exception.pauth.PauthExceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.observeai.platform.realtime.neutrino.exception.ErrorConstants.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PauthExceptionHandler extends ExtAppExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PauthExceptions.BaseException.class)
    public InternalAppErrorResponse handleException(PauthExceptions.BaseException e) {
        return getErrorDetailInstance(e);
    }

    @Override
    public void handleException(int rawStatusCode, String errorBody) {
        log.error("Pauth exception: {} with error body: {}", rawStatusCode, errorBody);
        InternalAppErrorResponse errorResponse = mapErrorBody(rawStatusCode, errorBody, InternalAppErrorResponse.class);

        // TODO: handle pauth exception based on status codes
        throw new PauthExceptions.BaseException(null, errorResponse);
    }

    private InternalAppErrorResponse getErrorDetailInstance(PauthExceptions.BaseException e) {
        return new InternalAppErrorResponse(INTERNAL_SERVER_ERROR, e.toString());
    }
}
