package com.observeai.platform.realtime.neutrino.data.twilio;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TwilioMessage {
    private String event;
    private int sequenceNumber;
    private TwilioMedia media;
    private String streamSid;
}
