package com.observeai.platform.realtime.neutrino.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ActiveSession {
    private String accountId;
    private String callId;
}
