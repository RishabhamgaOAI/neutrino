package com.observeai.platform.realtime.neutrino.service;

import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.data.store.CallRepository;
import com.observeai.platform.realtime.neutrino.util.Constants;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class CallStartMessageFilterStrategy implements RecordFilterStrategy<String, CallStartMessage> {
	private final CallRepository callRepository;

	@Override
	public boolean filter(ConsumerRecord<String, CallStartMessage> consumerRecord) {
		Header header = consumerRecord.headers().lastHeader(Constants.VENDOR_CALL_ID);
		String vendorCallId = new String(header.value(), StandardCharsets.UTF_8);
		for (Call call : callRepository.getCallsByVendorCallId(vendorCallId)) {
			if (call != null && call.isSecondaryStream())
				return false;
			}
		return true;
	}
}
