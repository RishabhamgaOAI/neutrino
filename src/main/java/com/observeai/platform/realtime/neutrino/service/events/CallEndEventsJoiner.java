package com.observeai.platform.realtime.neutrino.service.events;

import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallState;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.data.store.CallRepository;
import com.observeai.platform.realtime.neutrino.redis.CallEventsRedisStore;
import com.observeai.platform.realtime.neutrino.service.CallStateManager;
import com.observeai.platform.realtime.neutrino.util.CallEventJoinerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallEndEventsJoiner implements AbstractCallEventsJoiner {
    private final CallRepository callRepository;
    private final CallStateManager callStateManager;
    private final CallEventsRedisStore callEventsRedisStore;

    /**
     * Joins holdEvents in callEvent cache and callBackMetaEvent cache (if present)
     * and push startMessage if its complete
     */
    public void join(String vendorCallId, CallBackMetaEventDto callBackMetaEventDto) {
        Optional<CallEventDto> optionalCallEventDto = callEventsRedisStore.optionalGet(vendorCallId, CallEventType.START_EVENT.name());
        if (!CallEventJoinerUtil.preValidate(optionalCallEventDto, Optional.of(callBackMetaEventDto)))
            return;
        CallEventDto callEventDto = optionalCallEventDto.get();
        Set<Call> calls = callRepository.getCallsByObserveCallId(callEventDto.getObserveCallId());
        for (Call callToEnd : calls) {
            if (callToEnd != null) { // Call end has to be processed in this instance
                log.info("VendorCallId: {}, Processing call end event for vendorAccountId: {}, vendorAgentId: {}", callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto.getVendorAccountId(), callBackMetaEventDto.getVendorAgentId());
                switch (callBackMetaEventDto.getVendorName()) {
                    case "NICE":
                        callStateManager.updateState(callToEnd, CallState.ENDED);
                        callRepository.removeCall(callToEnd);
                        break;
                    case "FIVE9":
                        if(callEventDto.isReconnectionAllowed()){
                            if(callToEnd.getStartMessage().getAgentId() != null && callBackMetaEventDto.getObserveUserId()!=null &&  callToEnd.getStartMessage().getAgentId().equals(callBackMetaEventDto.getObserveUserId())){
                                callStateManager.updateState(callToEnd, CallState.ENDED);
                                callRepository.removeCall(callToEnd);
                            }
                            else{
                                log.info("VendorCallId: {}, Ignoring call back end event agentId: {} is not equal to call object agentId: {}, vendorAccountId: {}", callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto.getObserveUserId(), callToEnd.getStartMessage().getAgentId(), callBackMetaEventDto.getVendorAccountId());
                            }
                        } else {
                            log.info("VendorCallId: {}, Ignoring call back end event as reconnection is not allowed for vendorAccountId: {}, vendorAgentId: {}", callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto.getVendorAccountId(), callBackMetaEventDto.getVendorAgentId());
                        }
                        break;
                    default:
                        callStateManager.updateState(callToEnd, CallState.ENDED_FOR_TRANSFER);
                }
            }
        }
    }
}
