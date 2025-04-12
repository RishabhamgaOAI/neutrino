package com.observeai.platform.realtime.neutrino.data.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenesysIntegrationConfig {
    private boolean audioStreamAuthenticationEnabled;
    private String genesysStreamSecretKey;
}