package com.observeai.platform.realtime.neutrino.data.latency;

import com.observeai.platform.realtime.neutrino.service.newrelic.latency.LatencyProfiler;
import com.observeai.platform.realtime.neutrino.service.newrelic.latency.LatencyProfilerUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class GenesysLatencyProfilerStore {
	private final String sessionId;
	private final LatencyProfiler pingLatencyProfiler = LatencyProfilerUtil.getDefaultLatencyProfiler();
	private final LatencyProfiler processingLatencyProfiler = LatencyProfilerUtil.getDefaultLatencyProfiler();

	public GenesysLatencyProfilerStore(String sessionId) {
		this.sessionId = sessionId;
	}

	public void reportMetrics() {
		LatencyProfilerUtil.reportLatencyProfilerMetricsToNR(pingLatencyProfiler, "Genesys", sessionId, "ping_rtt");
		LatencyProfilerUtil.reportLatencyProfilerMetricsToNR(processingLatencyProfiler, "Genesys", sessionId, "processing");
	}
}
