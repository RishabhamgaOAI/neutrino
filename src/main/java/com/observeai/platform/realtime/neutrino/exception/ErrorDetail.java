package com.observeai.platform.realtime.neutrino.exception;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

@Getter
@Setter
@Data
public class ErrorDetail {
    String timestamp;
    String reason;
    String detailedMessage;

    public ErrorDetail(String reason, String detailedMessage) {
        this.reason = reason;
        this.detailedMessage = detailedMessage;
        this.timestamp = DateTime.now().toString();
    }
}
