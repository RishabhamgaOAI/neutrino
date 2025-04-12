package com.observeai.platform.realtime.neutrino.handler.callBackMetaEvent;

import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.redis.RedisValueStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallEndedMetaEventHandler implements CallBackMetaEventHandler {
    private final KafkaProducer kafkaProducer;
    private final KafkaProperties kafkaProperties;
    private final RedisValueStore redisValueStore;

    @Override
    public void onCallBackMetaEvent(CallBackMetaEventDto callBackMetaEventDto) {
        // persist in redis value store with ttl of 5 mins
        String key = callBackMetaEventDto.getVendorCallId() + "-" + callBackMetaEventDto.getObserveUserId() + "-" +
                callBackMetaEventDto.getCallEventType();
        redisValueStore.push(key, callBackMetaEventDto, Duration.ofMinutes(5));
        log.info("VendorCallId: {}, ObserveUserId: {}, push end event to redis value store with ttl of 5m",
                callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto.getObserveUserId());
        // Always broadcast call-end message for call-state change on the instance managing the call-object
        kafkaProducer.produceMessage(kafkaProperties.getTopics().getCallBackMetaEventsBroadcastTopic(),
                callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto);
    }
}
