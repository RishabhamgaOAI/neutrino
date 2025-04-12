package com.observeai.platform.realtime.neutrino.data.latency;

import lombok.AllArgsConstructor;
import java.util.Map;

@AllArgsConstructor
public class LatencyMetric {
	private final String vendor;
	private final String sessionId;
	private final String type;
	private final int count;
	private final int p100;
	private final int p99;
	private final int p95;

	public Map<String, Object> toMap() {
		return Map.of(
			"vendor", vendor,
			"sessionId", sessionId,
			"type", type,
			"count", count,
			"p100", p100,
			"p99", p99,
			"p95", p95
		);
	}
}
