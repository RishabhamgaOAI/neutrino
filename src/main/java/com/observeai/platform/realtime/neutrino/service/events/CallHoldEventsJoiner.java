package com.observeai.platform.realtime.neutrino.service.events;

import com.observeai.platform.realtime.commons.data.messages.CallEventResponse;
import com.observeai.platform.realtime.commons.data.messages.details.CallEvent;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventType;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.enums.CallStatus;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.redis.CallBackMetaEventsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallEventsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallStartMessagesRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallStatusRedisStore;
import com.observeai.platform.realtime.neutrino.util.CallEventJoinerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallHoldEventsJoiner implements AbstractCallEventsJoiner {
    private final CallEventsRedisStore callEventsRedisStore;
    private final CallStatusRedisStore callStatusRedisStore;
    private final CallStartMessagesRedisStore callStartMessagesRedisStore;
    private final CallBackMetaEventsRedisStore callBackMetaEventsRedisStore;
    private final KafkaProducer kafkaProducer;
    private final KafkaProperties kafkaProperties;

    /**
     * Joins holdEvents in callEvent cache and callBackMetaEvent cache (if present)
     * and push startMessage if its complete
     */
    public void join(String vendorCallId) {
        Optional<CallEventDto> optionalCallEventDto = callEventsRedisStore.optionalGet(vendorCallId, CallEventType.START_EVENT.name());
        Optional<CallBackMetaEventDto> optionalCallBackMetaEventDto = callBackMetaEventsRedisStore.optionalGet(vendorCallId, CallBackMetaEventType.HOLD_EVENT.name());
        if (!CallEventJoinerUtil.preValidate(optionalCallEventDto, optionalCallBackMetaEventDto))
            return;
        CallEventDto callEventDto = optionalCallEventDto.get();
	    log.info("VendorCallId: {}, Performing join between callEventDto and callBackMetaEvent for hold event", callEventDto.getVendorCallId());
        Optional<CallStartMessage> callStartMessageOptional = Optional.ofNullable(callStartMessagesRedisStore.get(callEventDto.getVendorCallId()));
        if (callStartMessageOptional.isEmpty()) {
            log.info("ObserveCallId: {}, VendorCallId: {}, Call start message not found for hold event{}",
                    callEventDto.getObserveCallId(), vendorCallId);
            return;
        }
        CallStartMessage startMessage = callStartMessageOptional.get();
        publishHoldMessage(callEventDto, startMessage);
    }

    private void publishHoldMessage(CallEventDto callEventDto, CallStartMessage startMessage){
        if (startMessage.isComplete()) {
            log.info("ObserveCallId: {}, Call hold message is complete after join. "
                    + "publishing hold message to kafka.", callEventDto.getObserveCallId());
            publishCallEvent(callEventDto.getObserveCallId(), callEventDto.getObserveAccountId(), callEventDto.getObserveUserId(), "HOLD");
            updateCallStatus(callEventDto.getObserveCallId());
        } else {
            log.warn("ObserveCallId: {}, Call start message is still partial even after join, ignoring hold message. StartMessage: {}",
                    callEventDto.getObserveCallId(), startMessage);
        }

    }

    private void updateCallStatus(String observecallId){
        callStatusRedisStore.push(observecallId, CallStatus.HOLD);
    }

    private void publishCallEvent(String callId, String accountId, String userId, String eventType){
        CallEvent callEvent = new CallEvent(eventType);
        CallEventResponse callEventResponse =  CallEventResponse.builder()
                .callId(callId)
                .accountId(accountId)
                .userId(userId)
                .message(callEvent).build();
        kafkaProducer.produceMessage(kafkaProperties.getTopics().getCallMessageTopic(), callId, callEventResponse);
    }
}
