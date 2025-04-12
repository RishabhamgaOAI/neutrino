package com.observeai.platform.realtime.neutrino.data.latency;

import com.observeai.platform.realtime.neutrino.service.newrelic.latency.LatencyProfiler;
import com.observeai.platform.realtime.neutrino.service.newrelic.latency.LatencyProfilerUtil;
import lombok.Getter;

@Getter
public class Five9LatencyProfilerStore {
	private final String sessionId;
	private final LatencyProfiler gatewayLatencyProfiler = LatencyProfilerUtil.getDefaultLatencyProfiler();
	private final LatencyProfiler processingLatencyProfiler = LatencyProfilerUtil.getDefaultLatencyProfiler();

	public Five9LatencyProfilerStore(String sessionId) {
		this.sessionId = sessionId;
	}

	public void reportMetrics() {
		LatencyProfilerUtil.reportLatencyProfilerMetricsToNR(gatewayLatencyProfiler, "FIVE9", sessionId, "gateway");
		LatencyProfilerUtil.reportLatencyProfilerMetricsToNR(processingLatencyProfiler, "FIVE9", sessionId, "processing");
	}
}
