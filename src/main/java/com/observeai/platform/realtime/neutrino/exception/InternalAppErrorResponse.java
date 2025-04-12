package com.observeai.platform.realtime.neutrino.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class InternalAppErrorResponse {
    @JsonProperty("error_code")
    String errorCode;
    @JsonProperty("error_description")
    String errorDescription;

    public InternalAppErrorResponse(String errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }
}
