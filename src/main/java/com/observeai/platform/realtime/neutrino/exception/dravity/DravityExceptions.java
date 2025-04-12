package com.observeai.platform.realtime.neutrino.exception.dravity;

import com.observeai.platform.realtime.neutrino.exception.InternalAppErrorResponse;
import lombok.Getter;

import static com.observeai.platform.realtime.neutrino.exception.ErrorConstants.*;

public class DravityExceptions {

    @Getter
    public static class BaseException extends RuntimeException {
        protected String errorCode;
        protected InternalAppErrorResponse errorResponse;

        public BaseException(String errorCode, InternalAppErrorResponse errorResponse) {
            super(errorCode);
            this.errorResponse = errorResponse;
            this.errorCode = errorCode;
        }

        @Override
        public String toString() {
            return "DravityException{" +
                    "errorCode='" + errorCode + '\'' +
                    ", errorResponse=" + errorResponse +
                    '}';
        }
    }

    public static class ResourceNotFoundException extends BaseException {

        public ResourceNotFoundException(InternalAppErrorResponse errorResponse) {
            super(RESOURCE_NOT_FOUND, errorResponse);
        }
    }

    public static class InternalServerErrorException extends BaseException {

        public InternalServerErrorException(InternalAppErrorResponse errorResponse) {
            super(INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    public static class BadRequestException extends BaseException {

        public BadRequestException(InternalAppErrorResponse errorResponse) {
            super(BAD_REQUEST, errorResponse);
        }
    }
}
