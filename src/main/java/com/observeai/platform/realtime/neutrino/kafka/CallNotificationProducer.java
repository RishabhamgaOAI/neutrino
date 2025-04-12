package com.observeai.platform.realtime.neutrino.kafka;

import com.newrelic.api.agent.Trace;
import com.observeai.platform.integration.commons.monitoring.MonitorEventType;
import com.observeai.platform.realtime.commons.data.messages.TagDetectionRequest;
import com.observeai.platform.realtime.commons.data.messages.notifications.*;
import com.observeai.platform.realtime.neutrino.client.MonitoringAgent;
import com.observeai.platform.realtime.neutrino.client.NotificationProperties;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallAttachedMetadata;
import com.observeai.platform.realtime.neutrino.data.CallState;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoConcise;
import com.observeai.platform.realtime.neutrino.data.dto.MetadataBasedProperties;
import com.observeai.platform.realtime.neutrino.observer.CallStateObserver;
import com.observeai.platform.realtime.neutrino.util.CallEventUtil;
import com.observeai.platform.realtime.neutrino.util.DravityRequestUtil;
import com.observeai.platform.realtime.neutrino.util.MonitoringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


import java.util.Optional;


@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Order(1)
public class CallNotificationProducer implements CallStateObserver {
    private final KafkaProperties kafkaProperties;
    private final KafkaProducer kafkaProducer;
    private final NotificationProperties notificationProperties;
    private final DravityRequestUtil dravityRequestUtil;
    private final MonitoringAgent monitoringAgent;
    private final TagDetectionRequestProducer tagDetectionRequestProducer;


    @Override
    public boolean isCriticalForProcessing() {
        return true;
    }

    @Override
    // @Trace(metricName = "CallNotificationProducer.onActiveProcessing()")
    public void onStarted(Call call) {
        if (!call.isCallStreamerCall())
            return;
        publishCallEventNotification(call, CallEventType.MONITORING_START_EVENT);
    }

    @Override
    public void onActiveProcessing(Call call) {
        publishCallEventNotification(call, CallEventType.START_EVENT);
    }

    @Override
    // @Trace(metricName = "CallNotificationProducer.onEnded()")
    public void onEnded(Call call) {
        if (call.getStateTransitions().contains(CallState.ACTIVE_PROCESSING) &&
                !call.getStateTransitions().contains(CallState.ENDED_FOR_TRANSFER)) {
            publishCallEventNotification(call, CallEventType.END_EVENT);
        } else if (call.isCallStreamerCall() && call.getStateTransitions().contains(CallState.STARTED)) {
            publishCallEventNotification(call, CallEventType.MONITORING_END_EVENT);
        }
    }

    private void publishCallEventNotification(Call call, CallEventType callEvent) {
        CallEventDto callEventDto = CallEventUtil.buildCallEventDto(call, callEvent);
        boolean isStartEvent = callEvent == CallEventType.START_EVENT || callEvent == CallEventType.MONITORING_START_EVENT;
    
        CallTopicEventNotification fullEventNotification = null;
        CallTopicEventNotification noMomentsEventNotification = null;
    
        if (isStartEvent) {
            AccountInfoConcise accountInfo = dravityRequestUtil.getAccountInfoByObserveAccountId(call.getStartMessage().getAccountId()).getResponse();
            
            CallAttachedMetadata callAttachedMetadata = new CallAttachedMetadata();
            callAttachedMetadata.setAccountInfo(accountInfo);
            
            setVendorFromCallSource(call, callAttachedMetadata);
            updateCallWithAccountInfo(call, callAttachedMetadata);
            
            fullEventNotification = new CallTopicStartNotification(callEventDto, notificationProperties, callAttachedMetadata);
            noMomentsEventNotification = new CallTopicStartNotification(callEventDto, notificationProperties, new CallAttachedMetadata());
    
            produceToTopic(kafkaProperties.getTopics().getNotificationServerTopic(), call, noMomentsEventNotification);
            produceToTopic(kafkaProperties.getTopics().getCallMessageTopic(), call, fullEventNotification);
            produceToTopic(kafkaProperties.getTopics().getCallAudioMessageTopic(), call, noMomentsEventNotification);
            
            if (callEvent == CallEventType.START_EVENT) {
                tagDetectionRequestProducer.publishTagDetectionRequest(call);
            }
        } else {
            fullEventNotification = new CallTopicEndNotification(callEventDto, notificationProperties);
            
            // Produce only for end events
            produceToTopic(kafkaProperties.getTopics().getNotificationServerTopic(), call, fullEventNotification);
            produceToTopic(kafkaProperties.getTopics().getCallMessageTopic(), call, fullEventNotification);
            produceToTopic(kafkaProperties.getTopics().getCallAudioMessageTopic(), call, fullEventNotification);
        }
    
        if (call.isMonitoringEnabled()) {
            sendMonitoringEvent(call, fullEventNotification, callEvent);
        }
    }
    
    private void produceToTopic(String topic, Call call, CallTopicEventNotification eventNotification) {
        log.info("ObserveCallId: {}, VendorCallId: {}, isPreviewCall: {}, Sending event to topic: {}.",
                call.getObserveCallId(),
                call.getStartMessage().getVendorCallId(),
                call.getStartMessage().isPreviewCall(),
                topic);
    
        kafkaProducer.produceMessage(topic, call.getObserveCallId(), eventNotification);
    }
    
    private void sendMonitoringEvent(Call call, CallTopicEventNotification fullEventNotification, CallEventType callEvent) {
        try {
            monitoringAgent.sendMonitoringEvent(
                    MonitoringUtil.createCallNotificationMonitoringParams(call, fullEventNotification, callEvent),
                    MonitorEventType.RTAA_USAGE_EVENTS,
                    call.getStartMessage().getDeploymentCluster());
        } catch (Exception e) {
            log.error("ObserveCallId: {}, Failed to send monitoring event for event: {}",
                    call.getObserveCallId(),
                    MonitoringUtil.getCallEventTypeString(callEvent),
                    e);
        }
    }

    public void setVendorFromCallSource(Call call, CallAttachedMetadata callAttachedMetadata) {
        AccountInfoConcise accountInfo = (callAttachedMetadata != null) ? callAttachedMetadata.getAccountInfo() : null;

        if (call.getVendor() != null && accountInfo != null) {
            accountInfo.setVendor(call.getVendor());
        }
    }
    private void updateCallWithAccountInfo(Call call, CallAttachedMetadata callAttachedMetadata) {
        Optional.ofNullable(callAttachedMetadata)
                .map(CallAttachedMetadata::getAccountInfo)
                .ifPresent(accountInfo -> {
                    call.setAccountName(accountInfo.getName());
                    call.setMonitoringEnabled(accountInfo.isMonitoringEnabled());
                    call.getStartMessage().setDeploymentCluster(accountInfo.getDeploymentCluster());
                });

        call.setMetadataBasedScriptsEnabled(Optional.ofNullable(callAttachedMetadata)
                .map(CallAttachedMetadata::getAccountInfo)
                .map(AccountInfoConcise::getMetadataBasedProperties)
                .map(MetadataBasedProperties::isMetadataBasedScriptsEnabled)
                .orElse(false));
    }

}


