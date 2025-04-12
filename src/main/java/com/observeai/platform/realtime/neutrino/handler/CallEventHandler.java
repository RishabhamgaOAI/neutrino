package com.observeai.platform.realtime.neutrino.handler;

import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.neutrino.redis.CallEventsRedisStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallEventHandler {
    private final CallEventsRedisStore callEventsRedisStore;

    public void onCallStartEvent(CallEventDto callEventDto) {
        log.info("ObserveCallId: {}, VendorCallId: {}, Received call event of type: START_EVENT",
            callEventDto.getObserveCallId(), callEventDto.getVendorCallId());
        callEventsRedisStore.push(callEventDto);
    }

    public void onCallEndEvent(CallEventDto callEventDto) {
        log.info("ObserveCallId: {}, VendorCallId: {}, Received call event of type: END_EVENT",
            callEventDto.getObserveCallId(), callEventDto.getVendorCallId());
        callEventsRedisStore.delete(callEventDto.getVendorCallId());
    }

    public void onCallEndForTransferEvent(CallEventDto callEventDto) {
        log.info("ObserveCallId: {}, VendorCallId: {}, Received call event of type: END_EVENT for transfer, " +
                        "Not removing related call events for handling possible future transfer events",
                callEventDto.getObserveCallId(), callEventDto.getVendorCallId());
    }
}
