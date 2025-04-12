package com.observeai.platform.realtime.neutrino.data;

import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
@AllArgsConstructor
public class CallMetadata {
    private String observeAccountId;
    private String observeUserId;
    private String observeCallId;
    private String vendorCallId;
    private Long startTime;
    private CallDirection direction;
    private boolean previewCall;
    private String experienceId;
}
