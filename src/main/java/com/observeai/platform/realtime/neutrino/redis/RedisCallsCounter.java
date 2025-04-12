package com.observeai.platform.realtime.neutrino.redis;

import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallState;
import com.observeai.platform.realtime.neutrino.observer.CallStateObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RedisCallsCounter implements CallStateObserver {
    private static final String ACTIVE_CALLS_COUNTER = "active-calls-counter";
    private final RedisTemplate<String, String> redisCounterTemplate;

    public void increment() {
        redisCounterTemplate.opsForValue().setIfAbsent(ACTIVE_CALLS_COUNTER, "0");
        redisCounterTemplate.opsForValue().increment(ACTIVE_CALLS_COUNTER);
    }

    public void decrement() {
        redisCounterTemplate.opsForValue().setIfAbsent(ACTIVE_CALLS_COUNTER, "0");
        redisCounterTemplate.opsForValue().decrement(ACTIVE_CALLS_COUNTER);
    }

    public Integer get() {
        String callCount = redisCounterTemplate.opsForValue().get(ACTIVE_CALLS_COUNTER);
        return callCount == null ? 0 : Integer.parseInt(callCount);
    }

    @Override
    public void onActiveProcessing(Call call) {
        increment();
    }

    @Override
    public void onEnded(Call call) {
        if (call.getStateTransitions().contains(CallState.ACTIVE_PROCESSING) &&
                !call.getStateTransitions().contains(CallState.ENDED_FOR_TRANSFER)) {
            decrement();
        }
    }
}
