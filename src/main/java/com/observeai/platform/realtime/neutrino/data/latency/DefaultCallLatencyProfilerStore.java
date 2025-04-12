package com.observeai.platform.realtime.neutrino.data.latency;

import com.observeai.platform.realtime.neutrino.service.newrelic.latency.LatencyProfiler;
import com.observeai.platform.realtime.neutrino.service.newrelic.latency.LatencyProfilerUtil;
import lombok.Getter;

@Getter
public class DefaultCallLatencyProfilerStore {
	private final String sessionId;
	private final LatencyProfiler pingLatencyProfiler = LatencyProfilerUtil.getDefaultLatencyProfiler();
	private final LatencyProfiler processingLatencyProfiler = LatencyProfilerUtil.getDefaultLatencyProfiler();

	public DefaultCallLatencyProfilerStore(String sessionId) {
		this.sessionId = sessionId;
	}

	public void reportMetrics(String vendor) {
		LatencyProfilerUtil.reportLatencyProfilerMetricsToNR(pingLatencyProfiler, vendor, sessionId, "ping_rtt");
		LatencyProfilerUtil.reportLatencyProfilerMetricsToNR(processingLatencyProfiler, vendor, sessionId, "processing");
	}

}
