package com.observeai.platform.realtime.neutrino.data.twilio;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TwilioMedia {
    private String track;
    private int chunk;
    private int timestamp;
    private String payload;
}
