package com.observeai.platform.realtime.neutrino.redis;

import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.neutrino.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Persists call events to redis store
 * push to redis store - when we receive the call start json message over websocket
 * remove from redis store - when the connection ends
 */
@Component
@Slf4j
public class CallEventsRedisStore extends RedisHashStoreByVendorCallId<CallEventDto> {

    public CallEventsRedisStore(RedisTemplate<String, String> redisHashStoreTemplate) {
        super(redisHashStoreTemplate, CallEventDto.class);
    }

    public void push(CallEventDto callEventDto) {
        log.info("VendorCallId: {}, Pushing callEvent of type {} to redis store", callEventDto.getVendorCallId(), callEventDto.getType());
        push(callEventDto.getVendorCallId(), callEventDto.getType().name(), callEventDto);
    }

    @Override
    protected String getKeySuffix() {
        return Constants.CALL_EVENTS;
    }
}
