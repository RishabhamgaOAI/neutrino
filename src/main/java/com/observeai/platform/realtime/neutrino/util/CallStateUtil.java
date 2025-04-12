package com.observeai.platform.realtime.neutrino.util;

import com.newrelic.api.agent.Trace;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.redis.LiveCallsRedisStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallStateUtil {
    private final LiveCallsRedisStore liveCallsRedisStore;

    // @Trace(metricName = "CallStateUtil.isCallInActiveProcessingState()")
    public boolean isCallInActiveProcessingState(Call call) {
        return liveCallsRedisStore.hasCall(call);
    }
}
