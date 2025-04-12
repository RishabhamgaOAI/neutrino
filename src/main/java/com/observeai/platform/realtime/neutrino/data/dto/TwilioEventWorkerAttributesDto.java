package com.observeai.platform.realtime.neutrino.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class TwilioEventWorkerAttributesDto {
    private String contactUri;
    private String fullName;
    private String imageUrl;
    private String email;
    private List<String> roles;
}
