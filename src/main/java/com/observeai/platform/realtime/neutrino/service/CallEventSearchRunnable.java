package com.observeai.platform.realtime.neutrino.service;

import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.neutrino.data.CallMetricsEvent;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.redis.CallBackMetaEventsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallEventsRedisStore;
import com.observeai.platform.realtime.neutrino.service.newrelic.CallMetricsCollector;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class CallEventSearchRunnable implements Runnable {
    private final CallEventsRedisStore callEventsRedisStore;
    private final CallBackMetaEventsRedisStore callBackMetaEventsRedisStore;
    private final CallBackMetaEventDto callBackMetaEventDto;
    private final CallMetricsCollector callMetricsCollector;

    @Override
    public void run() {
        log.info("vendorCallId={}, starting call event search runnable", callBackMetaEventDto.getVendorCallId());
        Optional<CallEventDto> callEventDto = callEventsRedisStore.optionalGet(callBackMetaEventDto.getVendorCallId(), CallEventType.START_EVENT.name());
        Long callBackMetaEventsCount = callBackMetaEventsRedisStore.getLength(callBackMetaEventDto.getVendorCallId());

        if (callEventDto.isEmpty() && callBackMetaEventsCount > 0) {
            log.error("vendorCallId={}, call stream not received within threshold", callBackMetaEventDto.getVendorCallId());
            CallMetricsEvent event = new CallMetricsEvent(null, callBackMetaEventDto.getObserveAccountId(), callBackMetaEventDto.getObserveUserId(),
                callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto.getVendorName(),
                callBackMetaEventDto.getVendorAccountId(), callBackMetaEventDto.getVendorAgentId(),
                callBackMetaEventDto.getArrivalTimestamp(), null, "MISSING_CALL_STREAM");
            callMetricsCollector.reportCallMetricsEvent(event);
        }
    }
}
