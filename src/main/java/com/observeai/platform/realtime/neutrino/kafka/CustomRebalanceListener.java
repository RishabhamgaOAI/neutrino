package com.observeai.platform.realtime.neutrino.kafka;

import com.newrelic.api.agent.NewRelic;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomRebalanceListener implements ConsumerAwareRebalanceListener {

    private final ThreadPoolTaskExecutor kafkaMetricsTaskExecutor;
    private final String PARTITION_REBALANCING_EVENT = "rt_partition_rebalancing_events";

    @Override
    public void onPartitionsAssigned(@NonNull Consumer<?, ?> consumer, @NonNull Collection<TopicPartition> topicPartitions) {
        onRebalancingEvent(consumer, topicPartitions, "Assigned");
    }

    @Override
    public void onPartitionsRevokedBeforeCommit(@NonNull Consumer<?, ?> consumer, @NonNull Collection<TopicPartition> topicPartitions) {
        onRebalancingEvent(consumer, topicPartitions, "Revoked");
    }

    @Override
    public void onPartitionsLost(@NonNull Consumer<?, ?> consumer, @NonNull Collection<TopicPartition> topicPartitions) {
        onRebalancingEvent(consumer, topicPartitions, "Lost");
    }

    private void onRebalancingEvent(Consumer<?, ?> consumer, Collection<TopicPartition> topicPartitions, String event) {
        log.info("Consumer: {}, Partitions {}: {}", event, consumer.groupMetadata().groupId(), topicPartitions);
        List<Map<String, Object>> assignedEvents = new LinkedList<>();
        topicPartitions.forEach(topicPartition -> {
            Map<String, Object> assignedEvent = new HashMap<>();
            assignedEvent.put("type", event);
            assignedEvent.put("topic", topicPartition.topic());
            assignedEvent.put("partition", topicPartition.partition());
            assignedEvent.put("consumerGroupId", consumer.groupMetadata().groupId());
            assignedEvents.add(assignedEvent);
        });

        log.info("Reporting Partitions {} to Newrelic: {}, {}", event, consumer.groupMetadata().groupId(), topicPartitions);
        reportRebalancingEvent(assignedEvents);
    }

    private void reportRebalancingEvent(List<Map<String, Object>> rebalancingEvents) {
        rebalancingEvents.forEach(eventMap -> {
            kafkaMetricsTaskExecutor.submit(() -> {
                try {
                    NewRelic.getAgent().getInsights().recordCustomEvent(PARTITION_REBALANCING_EVENT, eventMap);
                } catch (Throwable th) {
                    log.error("Failed to report metrics event={} to NewRelic with exception", PARTITION_REBALANCING_EVENT, th);
                }
            });
        });
    }
}
