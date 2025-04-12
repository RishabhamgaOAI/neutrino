package com.observeai.platform.realtime.neutrino.exception.handler;

import com.observeai.platform.realtime.neutrino.exception.InternalAppErrorResponse;
import com.observeai.platform.realtime.neutrino.exception.neutrino.AuthenticationFailureException;
import com.observeai.platform.realtime.neutrino.exception.neutrino.NeutrinoExceptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@RestControllerAdvice
@Slf4j
public class NeutrinoExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public InternalAppErrorResponse handleUnsupportedOperationException(NeutrinoExceptions.UnSupportedException e) {
        return getErrorDetailInstance(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InternalAppErrorResponse handleJsonParseException(NeutrinoExceptions.JsonParseException e) {
        return getErrorDetailInstance(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InternalAppErrorResponse handleUnknownExceptions(NeutrinoExceptions.UnhandledException e) {
        return getErrorDetailInstance(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InternalAppErrorResponse handleUserAccountException(NeutrinoExceptions.UserAccountException e) {
        return getErrorDetailInstance(e);
    }

    private InternalAppErrorResponse getErrorDetailInstance(NeutrinoExceptions.BaseException e) {
        return new InternalAppErrorResponse(e.getErrorCode(), e.getErrorDescription());
    }

    @ExceptionHandler(AuthenticationFailureException.class)
    public ResponseEntity<Object> handleAuthenticationFailureException(AuthenticationFailureException ex) {
        HashMap<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}
