package com.observeai.platform.realtime.neutrino.listeners;

import com.newrelic.api.agent.NewRelic;
import com.observeai.platform.realtime.neutrino.context.MdcFieldNames;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.handler.callBackMetaEvent.*;
import com.observeai.platform.realtime.neutrino.util.MDCUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallBackMetaEventListener {
    private final CallStartedMetaEventHandler callStartedMetaEventHandler;
    private final CallHoldMetaEventHandler callHoldMetaEventHandler;
    private final CallResumeMetaEventHandler callResumeMetaEventHandler;
    private final CallEndedMetaEventHandler callEndedMetaEventHandler;

    private final CallStartedBroadcastEventHandler callStartedBroadcastEventHandler;
    private final CallEndedBroadcastEventHandler callEndedBroadcastEventHandler;

    private final ThreadPoolTaskExecutor kafkaProcessingThreadPool;

    @KafkaListener(topics = "${kafka.topics.call-back-meta-events-topic}",
        groupId = "call-back-meta-events-consumer1", containerFactory = "callBackMetaEventListenerContainerFactory",
        autoStartup = "${kafka.consumer.auto-startup}")
    public void receiveCallBackMetaEvents(@Payload CallBackMetaEventDto callBackMetaEventDto) {
        try {
            HashMap<String, String> mdcContext = getMdcContext(callBackMetaEventDto);
            callBackMetaEventDto.setArrivalTimestamp(System.currentTimeMillis());
            kafkaProcessingThreadPool.submit(
                    MDCUtil.wrapWithMDC(() -> handleCallBackMetaEvent(callBackMetaEventDto), mdcContext)
            );
        } catch (Throwable th) {
            log.error("vendorCallId={}, exception while handling the callBackMetaEvent. event={}, error={}",
                callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto, th.toString(), th);
            NewRelic.noticeError(th);
        }
    }

    @KafkaListener(topics = "${kafka.topics.call-back-meta-events-broadcast-topic}",
            groupId = "${kafka.consumer.group-id.call-back-meta-events-broadcast-topic}",
            containerFactory = "callBackMetaEventListenerContainerFactory",
            autoStartup = "${kafka.consumer.auto-startup}")
    public void receiveCallBackMetaEventsBroadcast(@Payload CallBackMetaEventDto callBackMetaEventDto) {
        try {
            HashMap<String, String> mdcContext = getMdcContext(callBackMetaEventDto);
            callBackMetaEventDto.setArrivalTimestamp(System.currentTimeMillis());
            kafkaProcessingThreadPool.submit(
                    MDCUtil.wrapWithMDC(() -> handleCallBackMetaEventBroadcast(callBackMetaEventDto), mdcContext)
            );
        } catch (Throwable th) {
            log.error("vendorCallId={}, exception while handling the broadcast callBackMetaEvent. event={}, error={}",
                    callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto, th.toString(), th);
            NewRelic.noticeError(th);
        }
    }

    private void handleCallBackMetaEvent(CallBackMetaEventDto callBackMetaEventDto) {
        CallBackMetaEventHandler handler = getHandler(callBackMetaEventDto);
        if (handler != null) {
            handler.onCallBackMetaEvent(callBackMetaEventDto);
        } else {
            log.error("no handler found to handle callBackMetaEvent");
        }
    }

    private void handleCallBackMetaEventBroadcast(CallBackMetaEventDto callBackMetaEventDto) {
        log.info("VendorCallId: {}, VendorAgentId: {}, Received Broadcast callBackMetaEvent of type: {}",
                callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto.getVendorAgentId(), callBackMetaEventDto.getCallEventType());
        CallBackMetaEventHandler handler = getBroadcastHandler(callBackMetaEventDto);
        if (handler != null) {
            handler.onCallBackMetaEvent(callBackMetaEventDto);
        } else {
            log.warn("VendorCallId: {}, Unable to handle callBackMetaEvent with type: {}",
                    callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto.getCallEventType());
        }
    }

    private CallBackMetaEventHandler getHandler(CallBackMetaEventDto event) {
        switch (event.getCallEventType()) {
            case START_EVENT:
                return callStartedMetaEventHandler;
            case HOLD_EVENT:
                return callHoldMetaEventHandler;
            case RESUME_EVENT:
                return callResumeMetaEventHandler;
            case END_EVENT:
                return callEndedMetaEventHandler;
            default:
                return null;
        }
    }

    private CallBackMetaEventHandler getBroadcastHandler(CallBackMetaEventDto event) {
        switch (event.getCallEventType()) {
            case START_EVENT:
                return callStartedBroadcastEventHandler;
            case END_EVENT:
                return callEndedBroadcastEventHandler;
            default:
                return null;
        }
    }

    private HashMap<String, String> getMdcContext(CallBackMetaEventDto callBackMetaEventDto) {
        HashMap<String, String> mdcContext = new HashMap<>();
        mdcContext.put(MdcFieldNames.VENDOR_CALL_ID.getValue(), callBackMetaEventDto.getVendorCallId());
        mdcContext.put(MdcFieldNames.VENDOR_ACCOUNT_ID.getValue(), callBackMetaEventDto.getVendorAccountId());
        mdcContext.put(MdcFieldNames.VENDOR_USER_ID.getValue(), callBackMetaEventDto.getVendorAgentId());

        mdcContext.put(MdcFieldNames.OBSERVE_ACCOUNT_ID.getValue(), callBackMetaEventDto.getObserveAccountId());
        mdcContext.put(MdcFieldNames.OBSERVE_USER_ID.getValue(), callBackMetaEventDto.getObserveUserId());

        mdcContext.put(MdcFieldNames.CALL_BACK_META_EVENT_TYPE.getValue(), callBackMetaEventDto.getCallEventType().name());
        return mdcContext;
    }

}
