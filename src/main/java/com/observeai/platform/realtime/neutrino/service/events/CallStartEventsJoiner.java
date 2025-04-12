package com.observeai.platform.realtime.neutrino.service.events;

import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.neutrino.config.CallMetricsConfig;
import com.observeai.platform.realtime.neutrino.data.CallMetricsEvent;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventType;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaTopics;
import com.observeai.platform.realtime.neutrino.redis.CallBackMetaEventsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallEventsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallStartMessagesRedisStore;
import com.observeai.platform.realtime.neutrino.service.CallEventSearchRunnable;
import com.observeai.platform.realtime.neutrino.service.newrelic.CallMetricsCollector;
import com.observeai.platform.realtime.neutrino.util.CallEventJoinerUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.observeai.platform.realtime.neutrino.util.Constants.AVAYA;
import static com.observeai.platform.realtime.neutrino.util.Constants.UNKNOWN;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallStartEventsJoiner {
    private final CallEventsRedisStore callEventsRedisStore;
    private final CallBackMetaEventsRedisStore callBackMetaEventsRedisStore;
    private final CallStartMessagesRedisStore callStartMessagesRedisStore;
    private final KafkaProducer kafkaProducer;
    private final CallMetricsConfig callMetricsConfig;
    private final CallMetricsCollector callMetricsCollector;
    private final KafkaTopics kafkaTopics;
    private final ScheduledExecutorService callEventMetricsTaskExecutor;

    /**
     * Joins startEvents in callEvent cache and callBackMetaEvent cache (if present)
     * and persist startMessage in redis store and broadcast if its complete
     */
    public Optional<CallStartMessage> joinAndBroadcast(String vendorCallId) {
        return broadcast(join(vendorCallId));
    }

    public Optional<CallStartMessage> joinAndBroadcast(Optional<CallEventDto> optionalCallEventDto, Optional<CallBackMetaEventDto> optionalCallBackMetaEventDto) {
        Optional<CallStartMessage> startMessage = join(optionalCallEventDto, optionalCallBackMetaEventDto).filter(CallStartMessage::isComplete);
        return broadcast(startMessage);
    }

    private Optional<CallStartMessage> broadcast(Optional<CallStartMessage> startMessage) {
        if (startMessage.isPresent()) {
            kafkaProducer.produceMessage(kafkaTopics.getCallStartMessagesBroadcastTopic(), startMessage.get());
            callStartMessagesRedisStore.push(startMessage.get());
        }
        return startMessage;
    }


    private Optional<CallStartMessage> join(String vendorCallId) {
        Optional<CallEventDto> optionalCallEventDto = callEventsRedisStore.optionalGet(vendorCallId, CallEventType.START_EVENT.name());
        Optional<CallBackMetaEventDto> optionalCallBackMetaEventDto = callBackMetaEventsRedisStore.optionalGet(vendorCallId, CallBackMetaEventType.START_EVENT.name());
        return join(optionalCallEventDto, optionalCallBackMetaEventDto);
    }

    private Optional<CallStartMessage> join(Optional<CallEventDto> optionalCallEventDto, Optional<CallBackMetaEventDto> optionalCallBackMetaEventDto) {
        if (!CallEventJoinerUtil.preValidate(optionalCallEventDto, optionalCallBackMetaEventDto))
            return Optional.empty();
        CallEventDto callEventDto = optionalCallEventDto.get();
        CallBackMetaEventDto callBackMetaEventDto = optionalCallBackMetaEventDto.get();
        log.info("vendorCallId={}, performing join between callEventDto and callBackMetaEvent for start event", callEventDto.getVendorCallId());
        callEventDto.setObserveAccountId(callBackMetaEventDto.getObserveAccountId());
        callEventDto.setObserveUserId(callBackMetaEventDto.getObserveUserId());
        updateCallDirection(callEventDto, callBackMetaEventDto);
        if (callBackMetaEventDto.getVendorName() != null)
            callEventDto.setVendor(callBackMetaEventDto.getVendorName());
        CallStartMessage startMessage = CallStartMessage.fromCallEventDto(callEventDto);
        CallMetricsEvent event = new CallMetricsEvent(callEventDto.getObserveCallId(), callEventDto.getObserveAccountId(),
                callEventDto.getObserveUserId(), callEventDto.getVendorCallId(), callBackMetaEventDto.getVendorName(),
                callBackMetaEventDto.getVendorAccountId(), callBackMetaEventDto.getVendorAgentId(),
                callBackMetaEventDto.getArrivalTimestamp(), callEventDto.getTime(), "LATENCY");
        callMetricsCollector.reportCallMetricsEvent(event);
        return startMessage.isComplete() ? Optional.of(startMessage) : Optional.empty();
    }

    private void trackCallStreamMissingEvent(Optional<CallEventDto> callEvent, Optional<CallBackMetaEventDto> callBackMetaEvent) {
        if (callEvent.isEmpty() && callBackMetaEvent.isPresent() && StringUtils.hasLength(callBackMetaEvent.get().getVendorCallId())) {
            callEventMetricsTaskExecutor.schedule(new CallEventSearchRunnable(callEventsRedisStore, callBackMetaEventsRedisStore, callBackMetaEvent.get(), callMetricsCollector),
                    callMetricsConfig.getWaitForStreamInSeconds(), TimeUnit.SECONDS);
        }
    }

    String getVendorName(CallEventDto callEventDto, CallBackMetaEventDto callBackMetaEventDto) {
        return Optional.ofNullable(callBackMetaEventDto)
                .map(CallBackMetaEventDto::getVendorName)
                .orElseGet(() -> Optional.ofNullable(callEventDto)
                        .map(CallEventDto::getVendor)
                        .orElse("UNKNOWN"));
    }

    void updateCallDirection(CallEventDto callEventDto, CallBackMetaEventDto callBackMetaEventDto) {
        switch (getVendorName(callEventDto, callBackMetaEventDto).toUpperCase()) {
            case AVAYA:
                handleAvayaCallDirection(callEventDto, callBackMetaEventDto);
                break;
            default:
                callEventDto.setCallDirection(callBackMetaEventDto.getDirection());
        }
    }

    private void handleAvayaCallDirection(CallEventDto callEventDto, CallBackMetaEventDto callBackMetaEventDto) {
        if (isCallDirectionAlreadySet(callEventDto)) {
            log.info("ObserveCallId: {}, VendorCallId: {}, CallDirection is already set to {} for Vendor: {}",
                    callEventDto.getObserveCallId(), callEventDto.getVendorCallId(), callEventDto.getCallDirection(), AVAYA);
        } else {
            log.info("ObserveCallId: {}, VendorCallId: {}, Setting CallDirection to {} for Vendor: {}",
                    callEventDto.getObserveCallId(), callEventDto.getVendorCallId(), callBackMetaEventDto.getDirection(), AVAYA);
            callEventDto.setCallDirection(callBackMetaEventDto.getDirection());
        }
    }

    private boolean isCallDirectionAlreadySet(CallEventDto callEventDto) {
        return callEventDto.getCallDirection() == CallDirection.OUTBOUND ||
                callEventDto.getCallDirection() == CallDirection.INBOUND;
    }

}
