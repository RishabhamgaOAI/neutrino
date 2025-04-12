package com.observeai.platform.realtime.neutrino.listeners;

import com.observeai.platform.realtime.commons.data.messages.CallStreamerEvent;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallState;
import com.observeai.platform.realtime.neutrino.data.store.CallRepository;
import com.observeai.platform.realtime.neutrino.service.CallStateManager;
import com.observeai.platform.realtime.neutrino.util.CallStreamerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.observeai.platform.realtime.neutrino.util.Constants.CallStreamerConstants.*;


@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallStreamerEventListener {
	private final CallRepository callRepository;
	private final CallStateManager callStateManager;
	private final ThreadPoolTaskExecutor kafkaProcessingThreadPool;

	@KafkaListener(topics = "${kafka.topics.call-streamer-events-broadcast-topic}",
			groupId = "${kafka.consumer.group-id.call-streamer-events-broadcast-topic}",
			containerFactory = "callStreamerEventListenerContainerFactory",
			autoStartup = "${kafka.consumer.auto-startup}")
	public void receiveCallStreamerBroadcastEvent(@Payload CallStreamerEvent event) {
		kafkaProcessingThreadPool.submit(() -> {
			Set<Call> calls = callRepository.getCallsByObserveCallId(event.getObserveCallId());
			for (Call call : calls) {
				if (call != null && CallState.STARTED.equals(call.getState())) {
					log.info("ObserveCallId: {}, Processing call streamer event: {}", call.getObserveCallId(), event.getEvent());
					if (event.getEvent().equals(START_ANCHOR_MOMENT_DETECTED)) {
						call.setStartTime(System.currentTimeMillis());
						callStateManager.updateState(call, CallState.ACTIVE_PROCESSING);
						CallStreamerUtil.reportEventToNR(event);
					} else if (event.getEvent().equals(END_ANCHOR_MOMENT_DETECTED)) {
						callStateManager.updateState(call, CallState.ENDED);
						CallStreamerUtil.reportEventToNR(event);
					} else {
						log.error("ObserveCallId: {}, Unknown call streamer event: {}", call.getObserveCallId(), event.getEvent());
					}
				}
				CallStreamerUtil.reportEventToNR(event);
			}
		});
	}
}
