package com.observeai.platform.realtime.neutrino.client.asr.deepgram;

import com.observeai.platform.realtime.neutrino.client.DeepgramClient;
import com.observeai.platform.realtime.neutrino.client.DeepgramProperties;
import com.observeai.platform.realtime.neutrino.client.SlackClient;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.deepgram.DeepgramClientKey;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.util.Constants;
import com.observeai.platform.realtime.neutrino.util.DeepgramUtil;
import com.observeai.platform.realtime.neutrino.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class DeepgramClientProvider {
	private final ConcurrentHashMap<DeepgramClientKey, DeepgramClientPool> deepgramClientPools;
	private final KafkaProducer producer;
	private final KafkaProperties kafkaProperties;
	private final SlackClient slackClient;
	private final DeepgramProperties deepgramProperties;
	private final DeepgramUtil deepgramUtil;

	public DeepgramClient requestDeepgramClient(Call call) {
		DeepgramClientKey deepgramClientKey = new DeepgramClientKey(useOnPremDg(call), call.getCallSourceConfig());
		DeepgramClientPool deepgramClientPool = deepgramClientPools.get(deepgramClientKey);
		DeepgramClient deepgramClient = null;

		if (deepgramClientPool != null) {
			log.info("observeCallId={}, deepgram client pool found. borrowing client", call.getObserveCallId());
			deepgramClient = deepgramClientPool.borrowObject().orElse(null);
		}

		if (deepgramClient == null) {
			log.info("observeCallId={}, no deepgram client found. creating client manually", call.getObserveCallId());
			deepgramClient = new DeepgramClient(RandomUtil.random(), false, deepgramClientKey, producer, kafkaProperties, slackClient, deepgramProperties, deepgramUtil);
		}
		return deepgramClient;
	}

	public void returnDeepgramClient(Call call, DeepgramClient deepgramClient) {
		DeepgramClientKey deepgramClientKey = new DeepgramClientKey(useOnPremDg(call), call.getCallSourceConfig());
		DeepgramClientPool deepgramClientPool = deepgramClientPools.get(deepgramClientKey);
		if (deepgramClientPool != null && deepgramClient.isPartOfPool()) {
			deepgramClientPool.returnObject(deepgramClient);
		} else {
			log.info("deepgramClientId={}, observeCallId={}, deepgram client not part of pool. closing the session", deepgramClient.getId(), call.getObserveCallId());
			deepgramClient.close();
		}
	}

	private boolean useOnPremDg(Call call) {
		return call.getStartMessage().isPci() || Constants.BILL.equals(call.getStartMessage().getDeploymentCluster());
	}
}
