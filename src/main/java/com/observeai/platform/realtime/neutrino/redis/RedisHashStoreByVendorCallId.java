package com.observeai.platform.realtime.neutrino.redis;

import com.observeai.platform.realtime.neutrino.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class RedisHashStoreByVendorCallId<HV> extends RedisHashStore<HV> {

    public RedisHashStoreByVendorCallId(RedisTemplate<String, String> redisTemplate, Class<HV> hashValueClass) {
        super(redisTemplate, hashValueClass);
    }

    public final void push(String vendorCallId, String hashKey, HV value) {
        _push(constructKey(vendorCallId), hashKey, value);
    }

    public final Optional<HV> optionalGet(String vendorCallId, String hashKey) {
        return _optionalGet(constructKey(vendorCallId), hashKey);
    }

    public final Long getLength(String vendorCallId) {
        return _getLength(constructKey(vendorCallId));
    }

    public final void delete(String vendorCallId) {
        _delete(constructKey(vendorCallId));
    }

    public final List<HV> getAll(String vendorCallId) {
        return _getAll(constructKey(vendorCallId));
    }

    private String constructKey(String vendorCallId) {
        return Constants.VENDOR_CALL_ID + Constants.EQUALS
            + vendorCallId + Constants.SEMI_COLON + getKeySuffix();
    }

    protected abstract String getKeySuffix();
}
