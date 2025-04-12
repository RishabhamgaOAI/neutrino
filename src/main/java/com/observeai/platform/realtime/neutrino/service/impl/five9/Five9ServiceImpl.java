package com.observeai.platform.realtime.neutrino.service.impl.five9;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.commons.data.enums.CallMetadataType;
import com.observeai.platform.realtime.neutrino.data.dto.*;
import com.observeai.platform.realtime.neutrino.data.dto.five9.CallEvent;
import com.observeai.platform.realtime.neutrino.data.dto.five9.directive.DirectiveResponse;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionNotification;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionResponse;
import com.observeai.platform.realtime.neutrino.exception.dravity.DravityExceptions;
import com.observeai.platform.realtime.neutrino.exception.neutrino.NeutrinoExceptions;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.redis.CallMetadataRedisStore;
import com.observeai.platform.realtime.neutrino.service.five9.DirectiveService;
import com.observeai.platform.realtime.neutrino.service.five9.Five9Service;
import com.observeai.platform.realtime.neutrino.service.five9.SubscriptionService;
import com.observeai.platform.realtime.neutrino.util.CallBackMetaEventMapper;
import com.observeai.platform.realtime.neutrino.util.CallDirectionResolver.Five9CallDirectionResolver;
import com.observeai.platform.realtime.neutrino.util.ObjectMapperFactory;
import com.observeai.platform.realtime.neutrino.util.five9.Five9Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class Five9ServiceImpl implements Five9Service {
    private final KafkaProducer kafkaProducer;
    private final KafkaProperties kafkaProperties;
    private final SubscriptionService subscriptionService;
    private final DirectiveService directiveService;
    private final Five9Util five9Util;
    private final ObjectMapper objectMapper = ObjectMapperFactory.getSnakeCaseObjectMapper();
    private final Five9CallDirectionResolver directionResolver = new Five9CallDirectionResolver();
    private final CallMetadataRedisStore callMetadataRedisStore;

    @Override
    public SubscriptionResponse handleSubscriptionNotification(SubscriptionNotification notification) {
        switch (notification.getNotificationType()) {
            case "create":
                return createSubscription(notification);
            case "update":
                return updateSubscription(notification);
            case "delete":
                return deleteSubscription(notification);
            default:
                String error = String.format("Received notification with type %s. Allowed types: create/update/delete", notification.getNotificationType());
                throw new NeutrinoExceptions.UnSupportedException(error);
        }
    }

    @Override
    public void handleCallEvents(JsonNode jsonNode) {
        TreeNode attachedVariables = jsonNode.get("payload").get("attached_variables");
        CallEvent callEvent;
        try {
            callEvent = objectMapper.treeToValue(attachedVariables, CallEvent.class);
        } catch (JsonProcessingException ex) {
            log.error("Unable to convert five9 callEvent body to CallEvent.class");
            throw new NeutrinoExceptions.JsonParseException(ex.getMessage());
        }

        if (callEvent.getCallDetails() == null) {
            log.error("Invalid five9 call event. callDetails cannot be null. callEvent={}", callEvent);
            return;
        }

        String eventName = jsonNode.get("header").get("eventName").asText();
        log.info(String.format("Received five9 callEvent=%s for callId=%s, accountId=%s", eventName, callEvent.getCallDetails().getCallId(), callEvent.getCallDetails().getDomainId()));
        switch (eventName) {
            case "Accepted":
                handleCallAcceptedEvent(callEvent, jsonNode);
                log.info("Five9 call start event metadata: {}", jsonNode);
                break;
            case "AgentEnded":
                handleAgentEndedEvent(callEvent, jsonNode);
                break;
        }
    }

    public void handleCallAcceptedEvent(CallEvent callEvent, JsonNode jsonNode) {
        String five9CallId = callEvent.getCallDetails().getCallId();
        String domainId = callEvent.getCallDetails().getDomainId();
        String callId = five9CallId + "-" + domainId;
        String agentId = Optional.ofNullable(callEvent.getAgentDetails()).map(CallEvent.AgentDetails::getId).orElse(null);
        if (five9CallId == null || domainId == null || agentId == null) {
            log.error("Invalid five9CallEvent. callId/domainId/agentId cannot be null. callEvent={}", callEvent);
            return;
        }
        log.info("processing callAcceptedEvent. callEvent={}", callEvent);
        AccountAndUserInfoResponseDto response;
        try {
            response = five9Util.getAccountAndUserInfo(domainId, agentId);
        } catch (DravityExceptions.BaseException ex) {
            String errorMessage = String.format("Unable to process callConnectedEvent for five9CallId=%s from domainId=%s and agentId=%s due to upstream error: %s", five9CallId, domainId, agentId, ex.getErrorResponse().getErrorDescription());
            log.error(errorMessage);
            throw new NeutrinoExceptions.UserAccountException(errorMessage);
        }
        if(response.getAccountInfo().isReconnectionAllowed()){
            callId = five9CallId + "-" + domainId + "-" + agentId;
        }
        CallBackMetaEventDto callBackMetaEventDto = CallBackMetaEventDto.builder()
            .vendorName("FIVE9").vendorAccountId(domainId).vendorAgentId(agentId)
            .vendorCallId(callId).observeAccountId(response.getAccountInfo().getObserveAccountId())
            .observeUserId(response.getUserMapping().getObserveUserId())
            .callEventType(CallBackMetaEventType.START_EVENT)
            .direction(directionResolver.getCallDirection(callEvent))
            .build();

        kafkaProducer.produceMessage(kafkaProperties.getTopics().getCallBackMetaEventsTopic(), callId, callBackMetaEventDto);
//        kafkaProducer.produceProtoMessage(kafkaProperties.getTopics().getCallBackMetaEventsProtoTopic(), callId, CallBackMetaEventMapper.toProto(callBackMetaEventDto));

        CallMetadataDto metadata = CallMetadataDto.builder()
                .callId(callId).vendorAccountId(domainId)
                .observeAccountId(response.getAccountInfo().getObserveAccountId())
                .recordAudio(response.getAccountInfo().isRecordAudio())
                .agentId(agentId).observeUserId(response.getUserMapping().getObserveUserId())
                .startTime(0L).vendor(Five9Util.FIVE9_VENDOR_NAME)
                .callDetails(callEvent.getCallDetails()).agentDetails(callEvent.getAgentDetails())
                .build();
        if (kafkaProperties.isPushToCallMetadataTopic())
            kafkaProducer.produceMessageInCamelCase(kafkaProperties.getTopics().getCallMetadataTopic(), metadata);

        if(response.getAccountInfo().getMetadataBasedProperties()!=null && response.getAccountInfo().getMetadataBasedProperties().isMetadataBasedScriptsEnabled()){
            callMetadataRedisStore.push(callId, CallMetadataType.START_EVENT_METADATA, jsonNode);
        }
    }

    public void handleAgentEndedEvent(CallEvent five9CallEvent, JsonNode jsonNode) {
        log.info(five9CallEvent.toString());
        String five9CallId = five9CallEvent.getCallDetails().getCallId();
        String domainId = five9CallEvent.getCallDetails().getDomainId();
        String callId = five9CallId + "-" + domainId;
        String agentId = jsonNode.get("payload").get("AgentEnded").get("agent").get("agent_id").asText();

        if (five9CallId == null || domainId == null || agentId == null) {
            log.error("Invalid five9CallEvent. callId/domainId/agentId cannot be null. callEvent={}", five9CallEvent);
            return;
        }

        AccountAndUserInfoResponseDto response;
        try {
            response = five9Util.getAccountAndUserInfo(domainId, agentId);
        } catch (DravityExceptions.BaseException ex) {
            String errorMessage = String.format("Unable to process callConnectedEvent for five9CallId=%s from domainId=%s and agentId=%s due to upstream error: %s", five9CallId, domainId, agentId, ex.getErrorResponse().getErrorDescription());
            log.error(errorMessage);
            throw new NeutrinoExceptions.UserAccountException(errorMessage);
        }

        if(response.getAccountInfo().isReconnectionAllowed()){
            callId = five9CallId + "-" + domainId + "-" + agentId;
        }

        CallBackMetaEventDto callBackMetaEventDto = CallBackMetaEventDto.builder()
                .vendorName("FIVE9")
                .vendorAccountId(domainId)
                .vendorAgentId(agentId)
                .vendorCallId(callId)
                .observeAccountId(response.getAccountInfo().getObserveAccountId())
                .observeUserId(response.getUserMapping().getObserveUserId())
                .callEventType(CallBackMetaEventType.END_EVENT)
                .direction(directionResolver.getCallDirection(five9CallEvent))
                .build();

        publishFive9Event(callBackMetaEventDto);
    }

    private void publishFive9Event(CallBackMetaEventDto callBackMetaEventDto) {
        log.info("publishing FIVE9 call back event of type: {} for accountId={}, agentId={}, callId={}", callBackMetaEventDto.getCallEventType(), callBackMetaEventDto.getVendorAccountId(), callBackMetaEventDto.getVendorAgentId(), callBackMetaEventDto.getVendorCallId());
        kafkaProducer.produceMessage(kafkaProperties.getTopics().getCallBackMetaEventsTopic(), callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto);
//        kafkaProducer.produceProtoMessage(kafkaProperties.getTopics().getCallBackMetaEventsProtoTopic(), callBackMetaEventDto.getVendorCallId(), CallBackMetaEventMapper.toProto(callBackMetaEventDto));
        log.info("published FIVE9 call back event of type: {} for accountId={}, agentId={}, callId={}", callBackMetaEventDto.getCallEventType(), callBackMetaEventDto.getVendorAccountId(), callBackMetaEventDto.getVendorAgentId(), callBackMetaEventDto.getVendorCallId());
    }

    private SubscriptionResponse createSubscription(SubscriptionNotification notification) {
        String domainId = notification.getDomainId();
        SubscriptionResponse subscriptionResponse = subscriptionService.onCreate(notification);
        Optional<String> existingDirectiveId = directiveService.getDirectiveId(domainId);
        String directiveId = existingDirectiveId.orElseGet(() -> {
            DirectiveResponse response = directiveService.createDirective(domainId);
            five9Util.persistDirectiveId(response.getDirectiveId(), domainId);
            return response.getDirectiveId();
        });
        directiveService.attachDirectiveToSubscription(domainId, directiveId, notification.getSubscriptionId());
        subscriptionResponse.setDirectiveId(directiveId);

        return subscriptionResponse;
    }

    private SubscriptionResponse updateSubscription(SubscriptionNotification notification) {
        return subscriptionService.onUpdate(notification);
    }

    private SubscriptionResponse deleteSubscription(SubscriptionNotification notification) {
        return subscriptionService.onDelete(notification);
    }

}
