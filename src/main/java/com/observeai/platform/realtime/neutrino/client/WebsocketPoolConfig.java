package com.observeai.platform.realtime.neutrino.client;

import com.observeai.platform.realtime.neutrino.client.asr.deepgram.DeepgramClientPool;
import com.observeai.platform.realtime.neutrino.config.CallSourceConfig;
import com.observeai.platform.realtime.neutrino.config.CallSourceConfigs;
import com.observeai.platform.realtime.neutrino.config.DeepgramPoolProperties;
import com.observeai.platform.realtime.neutrino.config.IntegrationPoolConfigs;
import com.observeai.platform.realtime.neutrino.data.deepgram.DeepgramClientKey;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.util.Constants;
import com.observeai.platform.realtime.neutrino.util.DeepgramUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class WebsocketPoolConfig {
    private final DeepgramPoolProperties deepgramPoolProperties;
    private final DeepgramProperties deepgramProperties;
    private final KafkaProperties kafkaProperties;
    private final KafkaProducer producer;
    private final SlackClient slackClient;
    private final DeepgramUtil deepgramUtil;
    private final CallSourceConfigs callSourceConfigs;

    @Bean
    public ConcurrentHashMap<DeepgramClientKey, DeepgramClientPool> deepgramClientPools() throws InterruptedException {
        ConcurrentHashMap<DeepgramClientKey, DeepgramClientPool> deepgramClientPools = new ConcurrentHashMap<>();
        Field[] fields = Constants.CallSourceNameConstants.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                try {
                    String source = (String) field.get(null);
                    addDeepgramClientPool(deepgramClientPools, source, false);
                } catch (IllegalAccessException e) {
                    log.error("error while accessing field value. field={}", field.getName(), e);
                }
            }
        }
        log.info("completed initializing deepgram client pools");
        return deepgramClientPools;
    }

    private void addDeepgramClientPool(Map<DeepgramClientKey, DeepgramClientPool> pools, String source, boolean onPrem) throws InterruptedException {
        CallSourceConfig callSourceConfig = callSourceConfigs.getEntries().get(source).get("default");
        DeepgramClientKey clientKey = new DeepgramClientKey(onPrem, callSourceConfig);
        Optional<Integer> maxClients = Optional.ofNullable(deepgramPoolProperties).map(DeepgramPoolProperties::getProperties)
                .map(properties -> properties.get(source)).map(IntegrationPoolConfigs::getMaxClients);
        if (maxClients.isEmpty()) {
            log.warn("deepgram pool properties not found for source={}. skipping deepgram connection pool", source);
            return;
        }
        log.info("initializing pool for source={}, onPrem={} with maxClients={}", source, onPrem, maxClients.get());
        DeepgramClientPool pool;
        if (onPrem) {
            pool = new DeepgramClientPool(source + "-onprem-pool", maxClients.get(), deepgramProperties, kafkaProperties, producer, slackClient, deepgramUtil, clientKey);
        } else {
            pool = new DeepgramClientPool(source + "-oncloud-pool", maxClients.get(), deepgramProperties, kafkaProperties, producer, slackClient, deepgramUtil, clientKey);
        }
        pool.preparePool();
        pools.put(clientKey, pool);
    }
}
