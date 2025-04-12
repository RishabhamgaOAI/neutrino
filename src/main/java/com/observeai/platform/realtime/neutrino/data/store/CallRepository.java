package com.observeai.platform.realtime.neutrino.data.store;

import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CallRepository {
	private final ConcurrentHashMap<String, Set<Call>> callsByVendorCallId;
	private final ConcurrentHashMap<String, Set<Call>> callsByObserveCallId;

	public CallRepository() {
		this.callsByVendorCallId = new ConcurrentHashMap<>();
		this.callsByObserveCallId = new ConcurrentHashMap<>();
	}

	public Set<Call> getCallsByVendorCallId(String vendorCallId) {
		return Optional.ofNullable(callsByVendorCallId.get(vendorCallId)).orElse(Set.of());
	}

	public Set<Call> getCallsByObserveCallId(String observeCallId) {
		return Optional.ofNullable(callsByObserveCallId.get(observeCallId)).orElse(Set.of());
	}

	public Set<Call> getCalls() {
		return callsByObserveCallId.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
	}

	public void addCall(Call call) {
		callsByObserveCallId.computeIfAbsent(call.getObserveCallId(), k -> ConcurrentHashMap.newKeySet()).add(call);
		addCallToCallsByVendorCallIdMap(call);
		log.info("observeCallId={}, added call to store", call.getObserveCallId());
	}

	private void addCallToCallsByVendorCallIdMap(Call call) {
		Optional.ofNullable(call.getStartMessage()).map(CallStartMessage::getVendorCallId).ifPresent((vendorCallId) -> {
			callsByVendorCallId.computeIfAbsent(vendorCallId, k -> ConcurrentHashMap.newKeySet()).add(call);
		});
	}

	private void removeCallFromCallsByVendorCallIdMap(Call call) {
		Optional.ofNullable(call.getStartMessage()).map(CallStartMessage::getVendorCallId).ifPresent((vendorCallId) -> {
			callsByVendorCallId.get(vendorCallId).remove(call);
		});
	}

	public void updateObserveCallId(Call call, String observeCallId) {
		log.info("updating observeCallId of the call from {} to {}", call.getObserveCallId(), observeCallId);
		if (call.getObserveCallId() != null) {
			Set<Call> calls = callsByObserveCallId.get(call.getObserveCallId());
			if (calls != null)
				calls.remove(call);
		}
		call._setObserveCallId(observeCallId);
		callsByObserveCallId.computeIfAbsent(call.getObserveCallId(), k -> ConcurrentHashMap.newKeySet()).add(call);
	}

	public void updateCallStartMessage(Call call, CallStartMessage startMessage) {
		Optional<CallStartMessage> previousStartMessage = Optional.ofNullable(call.getStartMessage());
		if (Objects.equals(previousStartMessage.get(), startMessage))
			return;

		log.info("observeCallId={}, updating call start message", call.getObserveCallId());

		if (previousStartMessage.isPresent() && previousStartMessage.get().getVendorCallId() != startMessage.getVendorCallId())
			removeCallFromCallsByVendorCallIdMap(call);

		call._setStartMessage(startMessage);
	
		if (previousStartMessage.isEmpty() || previousStartMessage.get().getVendorCallId() != startMessage.getVendorCallId())
			addCallToCallsByVendorCallIdMap(call);

		if (!Objects.equals(call.getObserveCallId(), startMessage.getObserveCallId()))
			updateObserveCallId(call, startMessage.getObserveCallId());
	}

	public void removeCall(Call call) {
		callsByObserveCallId.get(call.getObserveCallId()).remove(call);
		Optional.ofNullable(call.getStartMessage()).map(CallStartMessage::getVendorCallId).ifPresent((vendorCallId) -> {
			callsByVendorCallId.get(vendorCallId).remove(call);
		});
		log.info("observeCallId={}, removed call from store. call states transitions={}", call.getObserveCallId(), call.getStateTransitions());
	}

	public int getCount() {
		return callsByObserveCallId.size();
	}

}
