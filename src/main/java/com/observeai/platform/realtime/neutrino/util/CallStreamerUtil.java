package com.observeai.platform.realtime.neutrino.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.commons.data.messages.CallStreamerEvent;
import com.observeai.platform.realtime.neutrino.data.callStreamer.CallStreamerSocketMessage;
import com.observeai.platform.realtime.neutrino.data.callStreamer.SocketMessage;
import com.observeai.platform.realtime.neutrino.service.newrelic.MetricsCollector;

import java.util.HashMap;
import java.util.Map;

public class CallStreamerUtil {
	private static final String CALL_STREAMER_METRIC = "rt_call_streamer_events";

	public static SocketMessage deserialize(String raw) throws JsonProcessingException {
		ObjectMapper mapper = ObjectMapperFactory.getSnakeCaseObjectMapper();
		SocketMessage base = mapper.readValue(raw, SocketMessage.class);
		if (base.getEvent() == null)
			base.setEvent(base.getType());
		switch (base.getEvent()) {
			case Constants.CallStreamerConstants.MANUAL_CALL_START:
			case Constants.CallStreamerConstants.MANUAL_CALL_END:
				return mapper.readValue(raw, CallStreamerSocketMessage.class);
			default:
				return base;
		}
	}

	public static void reportEventToNR(CallStreamerSocketMessage message, String callId) {
		Map<String, Object> metric = new HashMap<>();
		metric.put("observeAccountId", message.getObserveAccountId());
		metric.put("observeUserId", message.getObserveUserId());
		metric.put("observeCallId", callId);
		metric.put("source", "FRONTEND");
		metric.put("event", message.getEvent());
		MetricsCollector.reportMetricsAsEvent(CALL_STREAMER_METRIC, metric);
	}

	public static void reportEventToNR(CallStreamerEvent event) {
		Map<String, Object> metric = new HashMap<>();
		metric.put("observeAccountId", event.getObserveAccountId());
		metric.put("observeUserId", event.getObserveUserId());
		metric.put("observeCallId", event.getObserveCallId());
		metric.put("source", event.getSource());
		metric.put("event", event.getEvent());
		MetricsCollector.reportMetricsAsEvent(CALL_STREAMER_METRIC, metric);
	}
}
