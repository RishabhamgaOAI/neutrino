package com.observeai.platform.realtime.neutrino.service.events;

import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;

public interface AbstractCallEventsJoiner {
    default void join(String vendorCallId) {}

    default void join(String vendorCallId, CallBackMetaEventDto callBackMetaEventDto) {}
}
