package com.observeai.platform.realtime.neutrino.service;

import com.newrelic.api.agent.Trace;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.handler.CallEventHandler;
import com.observeai.platform.realtime.neutrino.observer.CallStateObserver;
import com.observeai.platform.realtime.neutrino.redis.CallStartMessagesRedisStore;
import com.observeai.platform.realtime.neutrino.util.CallEventUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallEventsProducer implements CallStateObserver {
    private final CallEventHandler callEventHandler;
    private final CallStartMessagesRedisStore startMessagesRedisStore;

    @Override
    public boolean isCriticalForProcessing() {
        return true;
    }

    @Override
    // @Trace(metricName = "CallEventsProducer.onStarted()")
    public void onStarted(Call call) {
        CallEventDto callEventDto = CallEventUtil.buildCallEventDto(call, CallEventType.START_EVENT);
        if (!call.getStartMessage().isComplete())
            log.info("observeCallId={}, call start message is partial without join", call.getObserveCallId());
        else {
            log.info("observeCallId={}, call start message is complete without join", call.getObserveCallId());
            CallStartMessage startMessage = CallStartMessage.fromCallEventDto(callEventDto);
            startMessagesRedisStore.push(startMessage);
        }
        callEventHandler.onCallStartEvent(callEventDto);
    }

    @Override
    public void onEnded(Call call) {
        CallEventDto callEventDto = CallEventUtil.buildCallEventDto(call, CallEventType.END_EVENT);
        callEventHandler.onCallEndEvent(callEventDto);
    }

    @Override
    public void onEndedForTransfer(Call call) {
        CallEventDto callEventDto = CallEventUtil.buildCallEventDto(call, CallEventType.END_EVENT);
        callEventHandler.onCallEndForTransferEvent(callEventDto);
    }
}
