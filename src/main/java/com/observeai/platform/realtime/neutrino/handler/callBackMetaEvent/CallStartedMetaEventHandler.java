package com.observeai.platform.realtime.neutrino.handler.callBackMetaEvent;

import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.redis.CallEventsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallBackMetaEventsRedisStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallStartedMetaEventHandler implements CallBackMetaEventHandler {
    private final KafkaProducer kafkaProducer;
    private final KafkaProperties kafkaProperties;
    private final CallEventsRedisStore callEventsRedisStore;
    private final CallBackMetaEventsRedisStore callBackMetaEventsRedisStore;

    @Override
    public void onCallBackMetaEvent(CallBackMetaEventDto callBackMetaEventDto) {
        callEventsRedisStore.optionalGet(callBackMetaEventDto.getVendorCallId(), CallEventType.START_EVENT.name())
                .ifPresentOrElse((callEventDto -> {
                    log.info("vendorCallId={}, callBackMetaEventType={}, pushing to broadcast topic for further processing",
                            callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto.getCallEventType());
                    kafkaProducer.produceMessage(kafkaProperties.getTopics().getCallBackMetaEventsBroadcastTopic(), callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto);
                }), () -> {
                    log.info("vendorCallId={}, callBackMetaEventType={}, no callEventDto found in redis. directly pushing to " +
                            "redis instead of broadcast", callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto.getCallEventType());
                    callBackMetaEventsRedisStore.push(callBackMetaEventDto);
                });
    }
}
