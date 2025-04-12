package com.observeai.platform.realtime.neutrino.exception.five9;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class HTTPError implements Serializable {

    @Getter
    @Setter
    @Data
    @AllArgsConstructor
    private static class HTTPErrorDetail implements Serializable {
        private String code;
        private String message;
        private String path;
        private String invalidValue;
    }

    private String traceId;
    private List<HTTPErrorDetail> details;

}
