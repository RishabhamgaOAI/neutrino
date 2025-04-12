package com.observeai.platform.realtime.neutrino.client.asr.deepgram;

import com.observeai.platform.realtime.neutrino.client.DeepgramClient;
import com.observeai.platform.realtime.neutrino.client.DeepgramProperties;
import com.observeai.platform.realtime.neutrino.client.SlackClient;
import com.observeai.platform.realtime.neutrino.data.deepgram.DeepgramClientKey;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.util.DeepgramUtil;
import com.observeai.platform.realtime.neutrino.util.RandomUtil;
import com.observeai.platform.realtime.neutrino.util.ThreadPoolUtil;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DeepgramClientPool {
    @Getter
    private final String name;
    private final DeepgramProperties deepgramProperties;
    private final KafkaProducer producer;
    private final KafkaProperties kafkaProperties;
    private final SlackClient slackClient;
    private final DeepgramUtil deepgramUtil;
    private final int capacity;
    private final AtomicInteger clientsInUse;
    private final ConcurrentLinkedDeque<DeepgramClient> pool;
    private final ScheduledExecutorService pingScheduler;
    private final ExecutorService retryExecutor;
    private final DeepgramClientKey deepgramClientKey;

    public DeepgramClientPool(String name, int capacity, DeepgramProperties deepgramProperties, KafkaProperties kafkaProperties, KafkaProducer producer, SlackClient slackClient, DeepgramUtil deepgramUtil, DeepgramClientKey deepgramClientKey) {
        this.name = name;
        this.deepgramProperties = deepgramProperties;
        this.kafkaProperties = kafkaProperties;
        this.producer = producer;
        this.slackClient = slackClient;
        this.deepgramUtil = deepgramUtil;
        this.capacity = capacity;
        this.clientsInUse = new AtomicInteger(0);
        this.pool = new ConcurrentLinkedDeque<>();
        this.deepgramClientKey = deepgramClientKey;
        this.retryExecutor = ThreadPoolUtil.createFixedThreadPool(10, name +"-retry-");
        this.pingScheduler = ThreadPoolUtil.createScheduledThreadPool(1, name + "-ping-");
        this.pingScheduler.scheduleAtFixedRate(sendKeepAlive(), 0, 4, java.util.concurrent.TimeUnit.SECONDS);
    }

    private Runnable sendKeepAlive() {
        return () -> {
            int numActive = 0;
            int numIdle = 0;
            Instant start = Instant.now();
            List<Callable<Void>> tasks = new ArrayList<>();
            try {
                for (DeepgramClient client : pool) {
                    if (client.isOpen()) {
                        client.sendKeepAliveMessage();
                        numActive++;
                    } else {
	                    tasks.add(() -> {
                            client.connect();
                            return null;
                        });
                        numIdle++;
                    }
                }
                if (!tasks.isEmpty()) {
                    retryExecutor.invokeAll(tasks);
                }
                log.info("deepgramPool={}, ping sent successfully to {} clients in {} millis. clients up for reconnection={}", name, numActive, Instant.now().toEpochMilli() - start.toEpochMilli(), numIdle);
            } catch (Exception e) {
                log.error("deepgramPool={}, error while sending ping to clients in pool. error={}", name, e.toString(), e);
            }
        };
    }

    public void preparePool() throws InterruptedException {
        ExecutorService executorService = ThreadPoolUtil.createFixedThreadPool(50, name + "-init-");
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < this.capacity; i++) {
            tasks.add(() -> {
                DeepgramClient client = buildDeepgramClient();
                this.addObject(client);
                return null;
            });
        }
        executorService.invokeAll(tasks);
        log.info("deepgramPool={}, initialized with {} clients", this.name, this.capacity);
        executorService.shutdown();
    }

    public Optional<DeepgramClient> borrowObject() {
        Optional<DeepgramClient> client = Optional.ofNullable(pool.pollFirst());
        if (client.isEmpty()) {
            log.error("deepgramPool={}, no more clients available in pool", name);
        } else {
            clientsInUse.getAndIncrement();
        }
        return client;
    }

    public void returnObject(DeepgramClient client) {
        clientsInUse.getAndDecrement();
        if (!client.isOpen()) {
            log.info("deepgramPool={}, deepgramClientId={}, returned client is not in open state. re-connecting and adding it to the pool", name, client.getId());
            retryExecutor.submit(() -> {
                client.connect();
                addObject(client);
            });
        } else {
            addObject(client);
        }
    }

    private void addObject(DeepgramClient client) {
        log.info("deepgramPool={}, deepgramClientId={}, adding deepgram client to pool", name, client.getId());
        pool.push(client);
    }

    @PreDestroy
    public void shutdown() {
        this.pingScheduler.shutdown();
        int counter = 0;
	    for (DeepgramClient client : pool) {
		    if (client.isOpen()) {
			    client.sendTextMessage("{\"type\":\"CloseStream\"}");
			    counter++;
		    }
	    }
        log.info("deepgramPool={}, close stream sent successfully to {} clients", name, counter);
    }

    private DeepgramClient buildDeepgramClient() {
        return new DeepgramClient(RandomUtil.random(), true, this.deepgramClientKey, this.producer, this.kafkaProperties, this.slackClient, this.deepgramProperties, deepgramUtil);
    }

    public int getClientsInUse() {
        return clientsInUse.get();
    }

    public int getClientsAvailable() {
        return pool.size();
    }
}
