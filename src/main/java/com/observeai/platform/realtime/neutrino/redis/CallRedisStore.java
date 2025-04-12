package com.observeai.platform.realtime.neutrino.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.util.ObjectMapperFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallRedisStore {
	private final ObjectMapper mapper = ObjectMapperFactory.getPascalCaseObjectMapper();
	private final RedisTemplate<String, String> redisCallStoreTemplate;

	public void persistForReconnection(Call call) throws JsonProcessingException {
		log.info("ObserveCallId: {}, Persisting call for reconnection", call.getObserveCallId());
		String callInString = mapper.writeValueAsString(call);
		redisCallStoreTemplate.opsForValue().set(constructKey(call.getObserveCallId()), callInString);
	}

	public boolean contains(String observeCallId) {
		return Boolean.TRUE.equals(redisCallStoreTemplate.hasKey(constructKey(observeCallId)));
	}

	public void delete(String observeCallId) {
		log.info("ObserveCallId: {}, Deleting call from redis store", observeCallId);
		redisCallStoreTemplate.delete(constructKey(observeCallId));
	}

	public Call getCallForReconnectionAndRemove(String observeCallId) {
		String callInString = redisCallStoreTemplate.opsForValue().get(constructKey(observeCallId));
		if (callInString == null) {
			log.error("ObserveCallId: {}, Call not found in redis", observeCallId);
			return null;
		}
		Call call = null;
		try {
			call = mapper.readValue(callInString, Call.class);
			delete(call.getObserveCallId());
		} catch (JsonProcessingException e) {
			log.error("ObserveCallId: {}, Error in parsing call from redis", observeCallId, e);
		}
		return call;
	}

	private String constructKey(String observeCallId) {
		return "call:" + observeCallId;
	}
}
