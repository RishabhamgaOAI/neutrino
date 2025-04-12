package com.observeai.platform.realtime.neutrino.redis;

import com.newrelic.api.agent.Trace;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallMetadata;
import com.observeai.platform.realtime.neutrino.observer.CallStateObserver;
import com.observeai.platform.realtime.neutrino.util.Constants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class LiveCallsRedisStore extends RedisHashStore<CallMetadata> implements CallStateObserver {

    public LiveCallsRedisStore(RedisTemplate<String, String> redisHashStoreTemplate) {
        super(redisHashStoreTemplate, CallMetadata.class);
    }

    @Override
    // @Trace(metricName = "LiveCallsRedisStore.onActiveProcessing()")
    public void onActiveProcessing(Call call) {
        CallMetadata metadata = new CallMetadata(call.getStartMessage().getAccountId(),
            call.getStartMessage().getAgentId(), call.getObserveCallId(),
            call.getStartMessage().getVendorCallId(), call.getStartTime(),
            call.getStartMessage().getDirection(),
            call.getStartMessage().isPreviewCall(),
                call.getStartMessage().getExperienceId());
        _push(constructKey(call), call.getObserveCallId(), metadata);
    }

    public CallMetadata get(Call call) {
        return _get(constructKey(call), call.getObserveCallId());
    }

    public void update(Call call, CallMetadata metadata) {
        _push(constructKey(call), call.getObserveCallId(), metadata);
    }

    public boolean hasCall(Call call) {
        return _has(constructKey(call), call.getObserveCallId());
    }


    @Override
    public void onEnded(Call call) {
        _delete(constructKey(call), call.getObserveCallId());
    }

    private String constructKey(Call call) {
        return constructKey(call.getStartMessage().getAccountId());
    }

    private String constructKey(String accountId) {
        return Constants.ACCOUNT_ID + Constants.EQUALS + accountId
            + Constants.SEMI_COLON + Constants.LIVE_CALLS_SUFFIX;
    }
}
