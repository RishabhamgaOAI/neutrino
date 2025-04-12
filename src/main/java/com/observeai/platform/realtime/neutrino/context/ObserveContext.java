package com.observeai.platform.realtime.neutrino.context;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ObserveContext {
    private String sessionId;
    private String observeCallId;
    private String secondaryCallId;
}
