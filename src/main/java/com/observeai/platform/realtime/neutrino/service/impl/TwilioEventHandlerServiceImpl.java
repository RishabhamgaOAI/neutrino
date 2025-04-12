package com.observeai.platform.realtime.neutrino.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.client.SlackClient;
import com.observeai.platform.realtime.neutrino.data.dto.*;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.service.TwilioEventHandlerService;
import com.observeai.platform.realtime.neutrino.util.CallBackMetaEventMapper;
import com.observeai.platform.realtime.neutrino.util.CallDirectionResolver.TwilioCallDirectionResolver;
import com.observeai.platform.realtime.neutrino.util.DravityRequestUtil;
import com.observeai.platform.realtime.neutrino.util.ObjectMapperFactory;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TwilioEventHandlerServiceImpl implements TwilioEventHandlerService {

    private final KafkaProducer producer;
    private final KafkaProperties kafkaProperties;
    private final DravityRequestUtil dravityRequestUtil;
    private final SlackClient slackClient;
    private static final ObjectMapper objectMapper = ObjectMapperFactory.getSnakeCaseObjectMapper();
    private static final TwilioCallDirectionResolver directionResovler = new TwilioCallDirectionResolver();
    private static final String TWILIO_VENDOR_NAME = "Twilio";
    private static final Integer TWILIO_AGENT_PARTICIPANT_ID = 1;

    @Override
    public void handleCallStartEvent(TwilioEventDto twilioEventDto) {
        final TwilioEventTaskAttributesDto taskAttributes;
        final TwilioEventWorkerAttributesDto workerAttributes;
        final HttpResponse<AccountAndUserInfoResponseDto> httpResponse;
        try {
            taskAttributes = objectMapper.readValue(twilioEventDto.getTaskAttributes(), TwilioEventTaskAttributesDto.class);
            workerAttributes = objectMapper.readValue(twilioEventDto.getWorkerAttributes(), TwilioEventWorkerAttributesDto.class);
            if (Objects.isNull(taskAttributes.getCallSid()) || Objects.isNull(taskAttributes.getAccountSid()) ||
                    Objects.isNull(twilioEventDto.getWorkerSid()) || Objects.isNull(taskAttributes.getDirection())) {
                String nullInfoErrorMessage = String.format("Received Twilio Reservation Created Event with incomplete metadata: " +
                        "twilioEvent=%s, taskAttributes=%s, workerAttributes=%s", twilioEventDto, taskAttributes, workerAttributes);
                log.info(nullInfoErrorMessage);
                slackClient.sendMessage(nullInfoErrorMessage);
                return;
            }
            log.info("Received Call Start Event for accountId={}, callId={}, AgentId={}, Direction={} with eventType={}", twilioEventDto.getAccountSid(), taskAttributes.getCallSid(), twilioEventDto.getWorkerSid(), taskAttributes.getDirection(), twilioEventDto.getEventType());
            httpResponse = dravityRequestUtil.getAccountAndUserInfo(TWILIO_VENDOR_NAME, twilioEventDto.getAccountSid(), twilioEventDto.getWorkerSid());
        } catch (IOException | URISyntaxException e) {
            log.error("dravity api exception. error={}", e.getMessage());
            return;
        }
        if (httpResponse.hasError()) {
            log.error("error in getting response from dravity for callId={}", taskAttributes.getCallSid(), httpResponse.getError().getCause());
            return;
        }

        // publish TwilioEvent and CallMetadata DTOs to respective kafka topics
        AccountAndUserInfoResponseDto responseDto = httpResponse.getResponse();
        twilioEventDto.setObserveAccountId(responseDto.getAccountInfo().getObserveAccountId());
        twilioEventDto.setObserveUserId(responseDto.getUserMapping().getObserveUserId());
        twilioEventDto.setDirection(directionResovler.getCallDirection(taskAttributes));
        publishTwilioEvent(twilioEventDto, taskAttributes.getCallSid(), responseDto);

        final CallMetadataDto metadata = new CallMetadataDto(
                taskAttributes.getCallSid(),
                taskAttributes.getAccountSid(),
                responseDto.getAccountInfo().getObserveAccountId(),
                responseDto.getAccountInfo().isRecordAudio(),
                twilioEventDto.getWorkerSid(),
                responseDto.getUserMapping().getObserveUserId(),
                0L,
                TWILIO_VENDOR_NAME,
                TWILIO_AGENT_PARTICIPANT_ID,
                taskAttributes,
                workerAttributes
        );

        if (kafkaProperties.isPushToCallMetadataTopic())
            producer.produceMessageInCamelCase(kafkaProperties.getTopics().getCallMetadataTopic(), metadata);
    }

    @Override
    public void handleCallEndEvent(TwilioEventDto twilioEventDto) {
        final TwilioEventTaskAttributesDto taskAttributes;
        final HttpResponse<AccountAndUserInfoResponseDto> httpResponse;
        try {
            taskAttributes = objectMapper.readValue(twilioEventDto.getTaskAttributes(), TwilioEventTaskAttributesDto.class);
            log.info("Received Call End Event for accountId={}, callId={} with eventType={}", twilioEventDto.getAccountSid(), taskAttributes.getCallSid(), twilioEventDto.getEventType());
        } catch (IOException e) {
            log.error("dravity api exception. error={}", e.getMessage());
        }
    }

    private void publishTwilioEvent(TwilioEventDto twilioEventDto, String callId, AccountAndUserInfoResponseDto accountAndUserInfo) {
        CallBackMetaEventDto callBackMetaEventDto = CallBackMetaEventMapper.from(twilioEventDto);
        callBackMetaEventDto.setVendorCallId(callId);

        producer.produceMessage(kafkaProperties.getTopics().getCallBackMetaEventsTopic(), callId, callBackMetaEventDto);
//        producer.produceProtoMessage(kafkaProperties.getTopics().getCallBackMetaEventsProtoTopic(), callId, CallBackMetaEventMapper.toProto(callBackMetaEventDto));
    }
}
