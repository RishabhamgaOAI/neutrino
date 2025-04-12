package com.observeai.platform.realtime.neutrino.config;

import com.observeai.platform.realtime.neutrino.kafka.CallNotificationProducer;
import com.observeai.platform.realtime.neutrino.observer.CallStateObserver;
import com.observeai.platform.realtime.neutrino.redis.CallBackMetaEventsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallStartMessagesRedisStore;
import com.observeai.platform.realtime.neutrino.redis.LiveCallsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.RedisCallsCounter;
import com.observeai.platform.realtime.neutrino.service.CallEventsProducer;
import com.observeai.platform.realtime.neutrino.service.DeepgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallStateManagerConfig {
    private final DeepgramService deepgramService;
    private final CallNotificationProducer callNotificationProducer;
    private final LiveCallsRedisStore liveCallsRedisStore;
    private final CallBackMetaEventsRedisStore callBackMetaEventsRedisStore;
    private final CallEventsProducer callEventsProducer;
    private final CallStartMessagesRedisStore callStartMessagesRedisStore;
    private final RedisCallsCounter redisCallsCounter;

    @Bean
    public List<CallStateObserver> observers() {
        List<CallStateObserver> observers = new ArrayList<>();
        observers.add(deepgramService);
        observers.add(callNotificationProducer);
        observers.add(callStartMessagesRedisStore);
        observers.add(liveCallsRedisStore);
        observers.add(callEventsProducer);
        observers.add(callBackMetaEventsRedisStore);
        observers.add(redisCallsCounter);
        return observers;
    }
}
