package com.observeai.platform.realtime.neutrino.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Getter
public class AgentAssistService {

    private final String streamSid;
    private final WebSocketSession session;
    private final KafkaConsumer<String, String> kafkaConsumer;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean shutdown = false;

    public AgentAssistService(String streamSid, WebSocketSession session,
                              KafkaConsumer<String, String> kafkaConsumer) {
        this.streamSid = streamSid;
        this.session = session;
        this.kafkaConsumer = kafkaConsumer;

        this.kafkaConsumer.subscribe(Collections.singleton(streamSid));
        executorService.submit(() -> this.poll(session));

    }

    public void poll(WebSocketSession session) {
        while (!shutdown) {
            if (!kafkaConsumer.subscription().isEmpty()) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        session.sendMessage(new TextMessage(record.value()));
                    } catch (IOException e) {
                        log.error("Error while sending websocket message to client. sessionId={}, message={}",
                                session.getId(), record.value(), e);
                    }
                }
                if (shutdown) {
                    kafkaConsumer.close();
                }
            }
        }
    }

    public void close() {
        this.shutdown = true;
        this.executorService.shutdown();
    }
}
