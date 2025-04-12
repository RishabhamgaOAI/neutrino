package com.observeai.platform.realtime.neutrino.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.commons.data.messages.CallTopicMessage;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.util.Constants;
import com.observeai.platform.realtime.neutrino.util.ObjectMapperFactory;
import com.observeai.platform.realtime.proto.CallBackMetaEventProto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, byte[]> byteArrayKafkaTemplate;
    private final ObjectMapper snakeCaseObjectMapper = ObjectMapperFactory.getSnakeCaseObjectMapper();
    private final ObjectMapper camelCaseObjectMapper = ObjectMapperFactory.getPascalCaseObjectMapper();

    public void produceMessage(String topicName, String key, String value, Iterable<Header> headers) {
        sendMessage(topicName, key, value, headers);
    }

    public void produceMessage(String topicName, CallStartMessage callStartMessage) {
        try {
            List<Header> headers = getMessageHeaders(callStartMessage);
            String message = snakeCaseObjectMapper.writeValueAsString(callStartMessage);
            sendMessage(topicName, callStartMessage.getVendorCallId(), message, headers);
        } catch (JsonProcessingException e) {
            log.error("failed to deserialize message to send to kafka topic due to error={}, e", e.toString(), e);
        }
    }

    public void produceMessage(String topicName, String key, Object value) {
        try {
            String message = snakeCaseObjectMapper.writeValueAsString(value);
            produceMessage(topicName, key, message, null);
        } catch (JsonProcessingException e) {
            log.error("failed to deserialize message to send to kafka topic");
        }
    }

    public void produceProtoMessage(String topic, String key, CallBackMetaEventProto.CallBackMetaEvent value) {
        sendMessage(topic, key, value.toByteArray(), null);
    }

    public void produceMessage(String topicName, String key, CallTopicMessage callTopicMessage) {
        try {
            List<Header> headers = getMessageHeaders(callTopicMessage);
            String message = snakeCaseObjectMapper.writeValueAsString(callTopicMessage);
            produceMessage(topicName, key, message, headers);
        } catch (JsonProcessingException e) {
            log.error("failed to deserialize message to send to kafka topic");
        }
    }

    private List<Header> getMessageHeaders(CallTopicMessage callTopicMessage) {
        return getMessageHeaders(callTopicMessage.getCallId(), callTopicMessage.getType().name());
    }

    private List<Header> getMessageHeaders(CallStartMessage callStartMessage) {
        return List.of(new RecordHeader(Constants.VENDOR_CALL_ID, callStartMessage.getVendorCallId().getBytes()));
    }

    private List<Header> getMessageHeaders(String callId, String messageType) {
        return Arrays.asList(
                new RecordHeader(Constants.CALL_ID, callId.getBytes()),
                new RecordHeader(Constants.MESSAGE_TYPE, messageType.getBytes()));
    }

    public void produceMessageInCamelCase(String topicName, String key, Object value) {
        try {
            String message = camelCaseObjectMapper.writeValueAsString(value);
            produceMessage(topicName, key, message, null);
        } catch (JsonProcessingException e) {
            log.error("failed to deserialize message to send to kafka topic");
        }
    }

    public void produceMessageInCamelCase(String topicName, Object value) {
        produceMessageInCamelCase(topicName, topicName, value);
    }

    private void sendMessage(String topicName, String key, String message, Iterable<Header> headers) {
        kafkaTemplate.send(new ProducerRecord<>(topicName, null, null, key, message, headers))
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Error sending message to kafka topic: {}", topicName, throwable);
                    }
                });
    }

    private void sendMessage(String topicName, String key, byte[] message, Iterable<Header> headers) {
        byteArrayKafkaTemplate.send(new ProducerRecord<>(topicName, null, null, key, message, headers))
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Error sending message to kafka topic: {}", topicName, throwable);
                    }
                });
    }


}
