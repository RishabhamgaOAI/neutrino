package com.observeai.platform.realtime.neutrino.data.dto.five9;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntrospectResponse {
    private boolean active;
    private String domainId;
    private String clientId;
    private String sub;
    private String iat;
    private String exp;
    private String iss;
    private String jti;
}
