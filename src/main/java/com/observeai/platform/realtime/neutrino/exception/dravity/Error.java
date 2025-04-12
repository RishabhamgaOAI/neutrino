package com.observeai.platform.realtime.neutrino.exception.dravity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class Error {
    String errorCode;
    String errorDescription;

    @JsonCreator
    public Error(@JsonProperty("error_code") String errorCode, @JsonProperty("error_description") String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }
}
