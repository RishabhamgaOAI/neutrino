package com.observeai.platform.realtime.neutrino.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActiveSessionRequestPayload {
    private String accountId;
    private String agentId;
}
