package com.observeai.platform.realtime.neutrino.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    private String applicationId;
    private String bootstrapServers;
    private String groupId;
    private String enableAutoCommit;
    private String autoCommitIntervalInMillis;
    private String sessionTimeoutInMillis;
    private String keyDeserializer;
    private String valueDeserializer;
    private String key;
    private String secret;
    private KafkaTopics topics;
    private int partitions;
    private short replicationFactor;
    private boolean pushToLatencyTopic;
    private boolean pushToCallMetadataTopic;
}