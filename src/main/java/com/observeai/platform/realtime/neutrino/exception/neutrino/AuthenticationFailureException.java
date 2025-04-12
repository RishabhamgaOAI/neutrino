package com.observeai.platform.realtime.neutrino.exception.neutrino;

import com.observeai.platform.realtime.neutrino.exception.ErrorConstants;

public class AuthenticationFailureException extends NeutrinoExceptions.BaseException {
    public AuthenticationFailureException(String errorDescription){
        super(ErrorConstants.UN_AUTHENTICATED, errorDescription);
    }
}
