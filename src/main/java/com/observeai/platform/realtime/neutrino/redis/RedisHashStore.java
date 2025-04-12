package com.observeai.platform.realtime.neutrino.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.util.ListUtil;
import com.observeai.platform.realtime.neutrino.util.ObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * redis store class for hash maps
 * @param <HV> hash value type
 */
@Slf4j
public class RedisHashStore<HV> {
    private final RedisTemplate<String, String> redisTemplate;
    private final Class<HV> hashValueClass;
    private final ObjectMapper mapper;

    public RedisHashStore(RedisTemplate<String, String> redisTemplate, Class<HV> hashValueClass) {
        this.redisTemplate = redisTemplate;
        this.hashValueClass = hashValueClass;
        this.mapper = ObjectMapperFactory.getPascalCaseObjectMapper();
    }

    protected final void _push(String key, String hashKey, HV value) {
        log.info("Pushing value to redis. key: {}, hashKey: {}", key, hashKey);
        try {
            String hashValueString = mapper.writeValueAsString(value);
            getOpsForHash().put(key, hashKey, hashValueString);
        } catch (JsonProcessingException e) {
            log.error("Unable to push value in redis hash store. Key: {}, HKey: {}, Value: {}", key, hashKey, value);
        }
    }

    protected final HV _get(String key, String hashKey) {
        try {
            Optional<String> value = Optional.ofNullable(getOpsForHash().get(key, hashKey));
            if (value.isPresent())
                return mapper.readValue(value.get(), hashValueClass);
        } catch (JsonProcessingException e) {
            log.error("Unable to get value from redis hash store. Key: {}, HKey: {}", key, hashKey);
        }
        return null;
    }

    protected final boolean _has(String key, String hashKey) {
        return Boolean.TRUE.equals(getOpsForHash().hasKey(key, hashKey));
    }

    protected final Optional<HV> _optionalGet(String key, String hashKey) {
        return Optional.ofNullable(_get(key, hashKey));
    }

    protected final List<HV> _getAll(String key) {
        List<String> rawValues = getOpsForHash().values(key);
        return ListUtil.emptyIfNull(rawValues).stream().map(v -> {
            try {
                return mapper.readValue(v, hashValueClass);
            } catch (JsonProcessingException e) {
                log.warn("Unable to convert redis hash value to string. value: {}", v);
                return null;
            }
        }).collect(Collectors.toList());
    }

    protected final void _delete(String key) {
        log.info("Deleting key from redis. key: {}", key);
        redisTemplate.delete(key);
    }

    protected final void _delete(String key, String hashKey) {
        log.info("Deleting hash key from redis. key: {}, hashKey: {}", key, hashKey);
        getOpsForHash().delete(key, hashKey);
    }

    protected final Long _getLength(String key) {
        log.info("Getting length of key from redis. key: {}", key);
        return getOpsForHash().size(key);
    }

    private HashOperations<String, String, String> getOpsForHash() {
        return redisTemplate.opsForHash();
    }
}
