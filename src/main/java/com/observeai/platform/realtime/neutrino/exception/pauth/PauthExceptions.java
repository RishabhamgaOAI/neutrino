package com.observeai.platform.realtime.neutrino.exception.pauth;

import com.observeai.platform.realtime.neutrino.exception.InternalAppErrorResponse;
import lombok.Getter;

public class PauthExceptions {

    @Getter
    public static class BaseException extends RuntimeException {
        protected String errorCode;
        protected InternalAppErrorResponse errorResponse;

        public BaseException(String errorCode, InternalAppErrorResponse errorResponse) {
            this.errorCode = errorCode;
            this.errorResponse = errorResponse;
        }

        @Override
        public String toString() {
            return "PauthException{" +
                    "errorCode='" + errorCode + '\'' +
                    ", errorResponse=" + errorResponse +
                    '}';
        }
    }
}
