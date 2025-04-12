package com.observeai.platform.realtime.neutrino.listeners;

import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.data.store.CallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class CallStartMessageBroadcastListener {
	private final CallRepository callRepository;

	@KafkaListener(topics = "${kafka.topics.call-start-messages-broadcast-topic}",
			groupId = "${kafka.consumer.group-id.call-start-messages-broadcast-topic}",
			containerFactory = "callStartMessageListenerContainerFactory",
			autoStartup = "${kafka.consumer.auto-startup}")
	public void onCallStartMessage(CallStartMessage callStartMessage) {
		for (Call call : callRepository.getCallsByVendorCallId(callStartMessage.getVendorCallId())) {
			if (call != null && call.isSecondaryStream() && !call.getStartMessage().isComplete()) {
				log.info("vendorCallId={}, setting start message in secondary stream", callStartMessage.getVendorCallId());
				callRepository.updateCallStartMessage(call, callStartMessage);
				String currentObserveCallId = call.getObserveCallId();
				if (!Objects.equals(currentObserveCallId, callStartMessage.getObserveCallId())) {
					callRepository.updateObserveCallId(call, callStartMessage.getObserveCallId());
					call.allowMediaMessageProcessing();
				}
			}
		}
	}
}
