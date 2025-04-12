package com.observeai.platform.realtime.neutrino.kafka;

import com.observeai.platform.realtime.commons.data.enums.MetadataMediumType;
import com.observeai.platform.realtime.commons.data.enums.MetadataSourceType;
import com.observeai.platform.realtime.commons.data.messages.TagDetectionMessage;
import com.observeai.platform.realtime.commons.data.messages.TagDetectionRequest;
import com.observeai.platform.realtime.neutrino.data.Call;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TagDetectionRequestProducer {
    private final KafkaProperties kafkaProperties;
    private final KafkaProducer kafkaProducer;

    public void publishTagDetectionRequest(Call call) {
        log.info("observeCallId={}, metadataBasedScriptsEnabled={}", call.getObserveCallId(), call.isMetadataBasedScriptsEnabled());
        if (!call.isMetadataBasedScriptsEnabled())
            return;

        // consider Five9 CCAAS start event metadata for tag detection
        Map<MetadataSourceType, List<MetadataMediumType>> metadataInfo = new HashMap<>();
        metadataInfo.put(MetadataSourceType.CCAAS, Collections.singletonList(MetadataMediumType.START_EVENT));

        TagDetectionMessage tagDetectionMessage = new TagDetectionMessage(call.getStartMessage().getVendorCallId(), metadataInfo);
        TagDetectionRequest tagDetectionRequest = TagDetectionRequest.builder()
                .accountId(call.getStartMessage().getAccountId())
                .userId(call.getStartMessage().getAgentId())
                .callId(call.getObserveCallId())
                .masterCallId(call.getMasterCallId())
                .parentCallId(call.getParentCallId())
                .message(tagDetectionMessage).build();

        log.info("ObserveCallId: {}, VendorCallId:{}, isPreviewCall:{}, Sending TagDetectionRequest event to CallMessage topic.",
                call.getObserveCallId(), call.getStartMessage().getVendorCallId(), call.getStartMessage().isPreviewCall());
        kafkaProducer.produceMessage(kafkaProperties.getTopics().getCallMessageTopic(), call.getObserveCallId(), tagDetectionRequest);
    }
}
