package com.observeai.platform.realtime.neutrino.redis;

import com.observeai.platform.realtime.commons.data.enums.CallMetadataType;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.observer.CallStateObserver;
import com.observeai.platform.realtime.neutrino.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class CallMetadataRedisStore extends RedisHashStoreByVendorCallId<Object> implements CallStateObserver {

    public CallMetadataRedisStore(RedisTemplate<String, String> redisHashStoreTemplate) {
        super(redisHashStoreTemplate, Object.class);
    }

    public void push(String vendorCallId, CallMetadataType metadataType, Object object) {
        log.info("VendorCallId: {}, Pushing callMetadata of type {} to redis store", vendorCallId, metadataType);
        push(vendorCallId, metadataType.name(), object);
    }

    @Override
    public void onEnded(Call call) {
        Optional.ofNullable(call.getStartMessage()).map(CallStartMessage::getVendorCallId)
                .ifPresent(vendorCallId -> {
                    log.info("vendorCallId={}, removing callMetadata from redis store if exists", vendorCallId);
                    delete(vendorCallId);
                });
    }

    @Override
    public void onEndedForTransfer(Call call) {
        log.info("VendorCallId: {}, Not removing callMetadata from redis store as call is being transferred",
                call.getStartMessage().getVendorCallId());
    }

    @Override
    protected String getKeySuffix() {
        return Constants.CALL_METADATA;
    }
}
