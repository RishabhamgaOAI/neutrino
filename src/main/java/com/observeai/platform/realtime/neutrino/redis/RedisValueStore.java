package com.observeai.platform.realtime.neutrino.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.util.ObjectMapperFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class RedisValueStore {
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper mapper = ObjectMapperFactory.getPascalCaseObjectMapper();

	public <V> void push(String key, V value, Duration ttl) {
		try {
			String valueAsString = mapper.writeValueAsString(value);
			redisTemplate.opsForValue().set(key, valueAsString, ttl);
		} catch (JsonProcessingException e) {
			log.error("Unable to convert value: {} to string, error={}", value, e.getMessage(), e);
		}
	}

	public <V> Optional<V> get(String key, Class<V> clazz) {
		String value = null;
		try {
			value = redisTemplate.opsForValue().get(key);
			if (value != null)
				return Optional.of(mapper.readValue(value, clazz));
		} catch (JsonProcessingException e) {
			log.error("Unable to convert value: {} to class: {}, error={}", value, clazz, e.getMessage(), e);
		}
		return Optional.empty();
	}
}
