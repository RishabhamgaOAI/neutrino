package com.observeai.platform.realtime.neutrino.redis;


import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.observer.CallStateObserver;
import com.observeai.platform.realtime.neutrino.util.Constants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CallStartMessagesRedisStore extends RedisHashStore<CallStartMessage> implements CallStateObserver {

    public CallStartMessagesRedisStore(RedisTemplate<String, String> redisHashStoreTemplate) {
        super(redisHashStoreTemplate, CallStartMessage.class);
    }

    public void push(CallStartMessage startMessage) {
        _push(constructKey(), startMessage.getVendorCallId(), startMessage);
    }

    public CallStartMessage get(String vendorCallId) {
        return _get(constructKey(), vendorCallId);
    }

    public Optional<CallStartMessage> optionalGet(String vendorCallId) {
        return _optionalGet(constructKey(), vendorCallId);
    }

    @Override
    public void onEnded(Call call) {
        Optional.ofNullable(call.getStartMessage()).map(CallStartMessage::getVendorCallId)
                .ifPresent(vendorCallId -> _delete(constructKey(), vendorCallId));
    }

    private String constructKey() {
        return Constants.START_MESSAGES;
    }
}
