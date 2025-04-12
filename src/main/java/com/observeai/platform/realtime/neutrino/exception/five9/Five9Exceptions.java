package com.observeai.platform.realtime.neutrino.exception.five9;

import lombok.Getter;

import static com.observeai.platform.realtime.neutrino.exception.ErrorConstants.*;


public class Five9Exceptions {

    @Getter
    public static class BaseException extends RuntimeException {
        protected String errorCode;
        protected Five9ErrorResponse errorResponse;

        public BaseException(String errorCode, Five9ErrorResponse errorResponse) {
            super(errorCode);
            this.errorResponse = errorResponse;
            this.errorCode = errorCode;
        }

        @Override
        public String toString() {
            return "Five9Exception{" +
                    "errorCode='" + errorCode + '\'' +
                    ", errorResponse=" + errorResponse +
                    '}';
        }
    }

    @Getter
    public static class BadRequestException extends BaseException {

        public BadRequestException(Five9ErrorResponse errorResponse) {
            super(BAD_REQUEST, errorResponse);
        }
    }


    @Getter
    public static class ForbiddenException extends BaseException {

        public ForbiddenException(Five9ErrorResponse errorResponse) {
            super(FORBIDDEN, errorResponse);
        }
    }

    @Getter
    public static class InternalServerErrorException extends BaseException {

        public InternalServerErrorException(Five9ErrorResponse errorResponse) {
            super(INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    @Getter
    public static class NotFoundException extends BaseException {

        public NotFoundException(Five9ErrorResponse errorResponse) {
            super(NOT_FOUND, errorResponse);
        }
    }

    @Getter
    public static class UnauthorizedException extends BaseException {

        public UnauthorizedException(Five9ErrorResponse errorResponse) {
            super(UNAUTHORIZED, errorResponse);
        }
    }
}
