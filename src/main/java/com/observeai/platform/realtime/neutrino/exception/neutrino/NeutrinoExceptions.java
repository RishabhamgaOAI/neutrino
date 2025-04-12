package com.observeai.platform.realtime.neutrino.exception.neutrino;

import lombok.Getter;
import lombok.Setter;

import static com.observeai.platform.realtime.neutrino.exception.ErrorConstants.*;

public class NeutrinoExceptions {

    @Getter
    @Setter
    public static class BaseException extends RuntimeException {
        String errorCode;
        String errorDescription;

        public BaseException(String errorCode, String errorDescription) {
            super(errorCode);
            this.errorCode = errorCode;
            this.errorDescription = errorDescription;
        }
    }

    @Getter
    @Setter
    public static class UnSupportedException extends BaseException {

        public UnSupportedException(String errorDescription) {
            super(UNSUPPORTED_OPERATION, errorDescription);
        }
    }

    @Getter
    @Setter
    public static class JsonParseException extends BaseException {

        public JsonParseException(String errorDescription) {
            super(JSON_PARSE_EXCEPTION, errorDescription);
        }
    }

    @Getter
    @Setter
    public static class UnhandledException extends BaseException {

        public UnhandledException(String errorDescription) {
            super(UNHANDLED_EXCEPTION, errorDescription);
        }
    }

    @Getter
    @Setter
    public static class UserAccountException extends BaseException {

        public UserAccountException(String errorDescription) {
            super(USER_ACCOUNT_EXCEPTION, errorDescription);
        }
    }

    @Getter
    @Setter
    public static class TopicCreationException extends BaseException {

        public TopicCreationException(String errorDescription) {
            super(TOPIC_CREATION_EXCEPTION, errorDescription);
        }
    }

    @Getter
    @Setter
    public static class TopicDeletionException extends BaseException {

        public TopicDeletionException(String errorDescription) {
            super(TOPIC_CREATION_EXCEPTION, errorDescription);
        }
    }


}
