package com.observeai.platform.realtime.neutrino.exception.five9;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class Five9ErrorResponse implements Serializable {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HTTPErrorDetail implements Serializable {
        private String code;
        private String message;
        private String path;
        private String invalidValue;
    }

    private String traceId;
    private List<HTTPErrorDetail> details;

}
