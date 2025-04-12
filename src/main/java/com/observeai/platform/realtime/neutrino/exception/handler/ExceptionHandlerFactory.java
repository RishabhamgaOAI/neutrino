package com.observeai.platform.realtime.neutrino.exception.handler;

import org.springframework.stereotype.Component;

@Component
public class ExceptionHandlerFactory {

    private static final Five9ExceptionHandler five9ExceptionHandler;
    private static final DravityExceptionHandler dravityExceptionHandler;
    private static final PauthExceptionHandler pauthExceptionHandler;

    static {
        five9ExceptionHandler = new Five9ExceptionHandler();
        dravityExceptionHandler = new DravityExceptionHandler();
        pauthExceptionHandler = new PauthExceptionHandler();
    }

    public static ExtAppExceptionHandler getFive9ExceptionHandler() {
        return five9ExceptionHandler;
    }

    public static ExtAppExceptionHandler getDravityExceptionHandler() {
        return dravityExceptionHandler;
    }

    public static ExtAppExceptionHandler getPauthExceptionHandler() {
        return pauthExceptionHandler;
    }
}
