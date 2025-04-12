package com.observeai.platform.realtime.neutrino.redis;

import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.observer.CallStateObserver;
import com.observeai.platform.realtime.neutrino.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class CallBackMetaEventsRedisStore extends RedisHashStoreByVendorCallId<CallBackMetaEventDto>
                                            implements CallStateObserver {

    public CallBackMetaEventsRedisStore(RedisTemplate<String, String> redisHashStoreTemplate) {
        super(redisHashStoreTemplate, CallBackMetaEventDto.class);
    }

    public void push(CallBackMetaEventDto callBackMetaEventDto) {
        log.info("VendorCallId: {}, Pushing callBackMetaEvent of type {} to redis store", callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto.getCallEventType());
        push(callBackMetaEventDto.getVendorCallId(), callBackMetaEventDto.getCallEventType().name(), callBackMetaEventDto);
    }

    @Override
    public void onEnded(Call call) {
        Optional.ofNullable(call.getStartMessage()).map(CallStartMessage::getVendorCallId)
                .ifPresent(vendorCallId -> {
                    log.info("vendorCallId={}, removing callBackMetaEvent from redis store if exists", vendorCallId);
                    delete(vendorCallId);
                });
    }

    @Override
    public void onEndedForTransfer(Call call) {
        log.info("VendorCallId: {}, Not removing callBackMetaEvent from redis store as call is being transferred",
                call.getStartMessage().getVendorCallId());
    }

    @Override
    protected String getKeySuffix() {
        return Constants.CALL_BACK_META_EVENTS;
    }
}
