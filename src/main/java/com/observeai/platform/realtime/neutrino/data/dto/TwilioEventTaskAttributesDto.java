package com.observeai.platform.realtime.neutrino.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class TwilioEventTaskAttributesDto {
    private String fromCountry;
    private String called;
    private String toCountry;
    private String toCity;
    private String type;
    private String toState;
    private String callerCountry;
    private String callSid;
    private String accountSid;
    private String fromZip;
    private String from;
    private String direction;
    private String calledZip;
    private String callerState;
    private String toZip;
    private String calledCountry;
    private String fromCity;
    private String calledCity;
    private String callerZip;
    private String apiVersion;
    private String calledState;
    private String fromState;
    private String caller;
    private String callerCity;
    private String name;
    private String to;
    private Conference conference;
}

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class Conference {
    private String sid;
    private Participants participants;
}

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class Participants {
    private String worker;
    private String customer;
}