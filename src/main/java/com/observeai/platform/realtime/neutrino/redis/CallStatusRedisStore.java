package com.observeai.platform.realtime.neutrino.redis;

import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.enums.CallStatus;
import com.observeai.platform.realtime.neutrino.observer.CallStateObserver;
import com.observeai.platform.realtime.neutrino.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CallStatusRedisStore extends RedisHashStore<CallStatus> implements CallStateObserver {

    public CallStatusRedisStore(RedisTemplate<String, String> redisHashStoreTemplate) {
        super(redisHashStoreTemplate, CallStatus.class);
    }

    public void push(String observeCallId, CallStatus callStatus) {
        _push(constructKey(), observeCallId, callStatus);
    }

    public CallStatus get(String observeCallId) {
        return _get(constructKey(), observeCallId);
    }

    @Override
    public void onEnded(Call call) {
        _delete(constructKey(), call.getObserveCallId());
    }

    private String constructKey() {
        return  Constants.CALL_STATUS;
    }
}
