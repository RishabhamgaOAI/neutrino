package com.observeai.platform.realtime.neutrino.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.data.dto.*;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.service.TalkdeskEventHandlerService;
import com.observeai.platform.realtime.neutrino.util.CallBackMetaEventMapper;
import com.observeai.platform.realtime.neutrino.util.CallDirectionResolver.TalkdeskCallDirectionResolver;
import com.observeai.platform.realtime.neutrino.util.DravityRequestUtil;
import com.observeai.platform.realtime.neutrino.util.ObjectMapperFactory;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TalkdeskEventHandlerServiceImpl implements TalkdeskEventHandlerService{

    private final KafkaProducer producer;
    private final KafkaProperties kafkaProperties;
    private final DravityRequestUtil dravityRequestUtil;
    private static final ObjectMapper objectMapper = ObjectMapperFactory.getSnakeCaseObjectMapper();
    private static final TalkdeskCallDirectionResolver directionResolver = new TalkdeskCallDirectionResolver();
    public static final String TALKDESK_VENDOR_NAME = "Talkdesk";
    private static final Integer AGENT_PARTICIPANT_ID = 1;

    @Override
    public void handleCallStartEvent(TalkdeskEventDto talkdeskEventDto) {
        final TalkdeskCallDto callDetails;
        final TalkdeskAgentDto agentDetails;
        final HttpResponse<AccountAndUserInfoResponseDto> httpResponse;
        try {
            callDetails = objectMapper.readValue(objectMapper.writeValueAsString(talkdeskEventDto), TalkdeskCallDto.class);
            agentDetails = objectMapper.readValue(objectMapper.writeValueAsString(talkdeskEventDto), TalkdeskAgentDto.class);
            log.info("Received Call Start Event for accountId={}, callId={} with eventType={}", talkdeskEventDto.getAccountId(), callDetails.getInteractionId(), talkdeskEventDto.getEventType());
            httpResponse = dravityRequestUtil.getAccountAndUserInfo(TALKDESK_VENDOR_NAME, talkdeskEventDto.getAccountId(), talkdeskEventDto.getAgentId());
        } catch (IOException | URISyntaxException e) {
            log.error("dravity api exception. error={}", e.getMessage());
            return;
        }
        if (httpResponse.hasError()) {
            log.error("error in getting response from dravity for callId={}", callDetails.getInteractionId(), httpResponse.getError().getCause());
            return;
        }

        // publish TalkdeskEvent and CallMetadata DTOs to respective kafka topics
        AccountAndUserInfoResponseDto responseDto = httpResponse.getResponse();
        talkdeskEventDto.setObserveAccountId(responseDto.getAccountInfo().getObserveAccountId());
        talkdeskEventDto.setObserveUserId(responseDto.getUserMapping().getObserveUserId());
        talkdeskEventDto.setDirection(directionResolver.getCallDirection(callDetails));
        publishTalkdeskEvent(talkdeskEventDto, callDetails.getInteractionId());

        final CallMetadataDto metadata = new CallMetadataDto(
                talkdeskEventDto.getInteractionId(),
                talkdeskEventDto.getAccountId(),
                responseDto.getAccountInfo().getObserveAccountId(),
                responseDto.getAccountInfo().isRecordAudio(),
                talkdeskEventDto.getAgentId(),
                responseDto.getUserMapping().getObserveUserId(),
                0L,
                TALKDESK_VENDOR_NAME,
                AGENT_PARTICIPANT_ID,
                callDetails,
                agentDetails
        );

        if (kafkaProperties.isPushToCallMetadataTopic())
            producer.produceMessageInCamelCase(kafkaProperties.getTopics().getCallMetadataTopic(), metadata);
    }

    @Override
    public void handleCallEndEvent(TalkdeskEventDto talkdeskEventDto) {
        final TalkdeskCallDto callDetails;
        try {
            callDetails = objectMapper.readValue(objectMapper.writeValueAsString(talkdeskEventDto), TalkdeskCallDto.class);
            log.info("Received Call End Event for accountId={}, callId={} with eventType={}", talkdeskEventDto.getAccountId(), callDetails.getInteractionId(), talkdeskEventDto.getEventType());
        } catch (IOException e) {
            log.error("dravity api exception. error={}", e.getMessage());
        }
    }

    private void publishTalkdeskEvent(TalkdeskEventDto talkdeskEventDto, String callId) {
        CallBackMetaEventDto callBackMetaEventDto = CallBackMetaEventMapper.from(talkdeskEventDto);
        callBackMetaEventDto.setVendorCallId(callId);

        producer.produceMessage(kafkaProperties.getTopics().getCallBackMetaEventsTopic(), callId, callBackMetaEventDto);
//        producer.produceProtoMessage(kafkaProperties.getTopics().getCallBackMetaEventsProtoTopic(), callId, CallBackMetaEventMapper.toProto(callBackMetaEventDto));
    }
}
