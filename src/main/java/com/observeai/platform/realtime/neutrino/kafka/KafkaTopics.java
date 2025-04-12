package com.observeai.platform.realtime.neutrino.kafka;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@Slf4j
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopics {
    private String callMessageTopic;
    private String callAudioMessageTopic;
    private String callMetadataTopic;
    private String notificationServerTopic;
    private String callBackMetaEventsTopic;
    private String callBackMetaEventsProtoTopic;
    private String callBackMetaEventsBroadcastTopic;
    private String callStreamerEventsBroadcastTopic;
    private String latencyCallMessageTopic;
    private String callStartMessagesBroadcastTopic;

    public List<String> getAllTopicNames() {
        List<String> names = new ArrayList<>();
        for (Field f : this.getClass().getDeclaredFields()) {
            if (!f.getName().contains("Topic"))
                continue;
            try {
                names.add((String) f.get(this));
            } catch (IllegalAccessException ex) {
                log.error("Exception while fetching kafka topic names.", ex);
            }
        }
        return names;
    }
}
