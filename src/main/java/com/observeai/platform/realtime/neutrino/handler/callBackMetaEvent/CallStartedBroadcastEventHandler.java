package com.observeai.platform.realtime.neutrino.handler.callBackMetaEvent;

import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallState;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventType;
import com.observeai.platform.realtime.neutrino.data.store.CallRepository;
import com.observeai.platform.realtime.neutrino.redis.CallBackMetaEventsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallEventsRedisStore;
import com.observeai.platform.realtime.neutrino.service.CallStateManager;
import com.observeai.platform.realtime.neutrino.service.events.CallStartEventsJoiner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;

import static com.observeai.platform.realtime.neutrino.util.Constants.CALL_ID_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallStartedBroadcastEventHandler implements CallBackMetaEventHandler {
    private final CallBackMetaEventsRedisStore callBackEventsRedisStore;
    private final CallEventsRedisStore callEventsStore;
    private final CallStateManager callStateManager;
    private final CallRepository callRepository;
    private final CallStartEventsJoiner callStartEventsJoiner;

    @Override
    public void onCallBackMetaEvent(CallBackMetaEventDto callBackMetaEventDto) {
        Set<Call> calls = callRepository.getCallsByVendorCallId(callBackMetaEventDto.getVendorCallId());
        calls.removeIf(call -> call == null || !call.isPrimaryStream());

        if (calls.isEmpty()) {
            log.debug("no primary streams found in this instance. skipping the event");
            return;
        } else {
            log.info("primary stream found in this instance");
        }

        Optional<CallEventDto> callEventDto = callEventsStore.optionalGet(callBackMetaEventDto.getVendorCallId(), CallEventType.START_EVENT.name());
        if (callEventDto.isEmpty()) {
            log.error("no callEvent found in redis of type={}. not processing the event", CallEventType.START_EVENT.name());
            return;
        }

        log.info("started processing callBackMetaEvent");
        Optional<CallBackMetaEventDto> previousCallBackMetaEventDto = callBackEventsRedisStore.optionalGet(callBackMetaEventDto.getVendorCallId(), CallBackMetaEventType.START_EVENT.name());
        if (previousCallBackMetaEventDto.isPresent())
            log.info("previous callBackEvent of type={} found in redis. event={}", CallBackMetaEventType.START_EVENT.name(), previousCallBackMetaEventDto.get());

        for (Call call : calls) {
            callBackEventsRedisStore.push(callBackMetaEventDto);
            if (previousCallBackMetaEventDto.isPresent() && isTransferCase(previousCallBackMetaEventDto.get(), callBackMetaEventDto)) {
                log.info("transfer case detected. ending call for transfer");
                callStateManager.updateState(call, CallState.ENDED_FOR_TRANSFER);
                callRepository.removeCall(call);
                // Set up child call for transfer case
                setupTransferChildCall(call);
            } else if (CallState.STARTED.equals(call.getState())) {
                log.info("observeCallId={}, attempting events join", call.getObserveCallId());
                callStartEventsJoiner.joinAndBroadcast(callEventDto, Optional.of(callBackMetaEventDto)).ifPresent((startMessage) -> {
                    callRepository.updateCallStartMessage(call, startMessage);
                    callStateManager.updateState(call, CallState.ACTIVE_PROCESSING);
                    call.allowMediaMessageProcessing();
                    log.info("observeCallId={}, allowed media message processing", call.getObserveCallId());
                });
            }
        }
    }

    private boolean isTransferCase(CallBackMetaEventDto previousCallBackMetaEvent, CallBackMetaEventDto newCallBackMetaEvent) {
        return !newCallBackMetaEvent.getVendorAgentId().equals(previousCallBackMetaEvent.getVendorAgentId());
    }

    private void setupTransferChildCall(Call previousCall) {
        // Build child Call object
        String callId = CALL_ID_PREFIX + UUID.randomUUID();
        Call call = previousCall.createChildCall(callId);
        log.info("assigned {} as child observeCallId for parent observeCallId={}, conn with sessionId={}", callId,
                call.getParentCallId(), call.getCallAudioSession().getId());
        callRepository.addCall(call);

        // Update states for child call
        callStateManager.updateState(call, CallState.CONNECTION_ESTABLISHED);
        if (previousCall.getStateTransitions().contains(CallState.STARTED)) {
            callStateManager.updateState(call, CallState.STARTED);
        }
    }
}
