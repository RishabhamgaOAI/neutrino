package com.observeai.platform.realtime.neutrino.handler.callBackMetaEvent;

import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.service.events.CallEndEventsJoiner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallEndedBroadcastEventHandler implements CallBackMetaEventHandler {
    private final CallEndEventsJoiner eventsJoiner;

    @Override
    public void onCallBackMetaEvent(CallBackMetaEventDto callBackMetaEventDto) {
        eventsJoiner.join(callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto);
    }
}
