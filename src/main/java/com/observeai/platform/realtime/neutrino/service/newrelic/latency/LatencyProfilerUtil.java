package com.observeai.platform.realtime.neutrino.service.newrelic.latency;

import com.observeai.platform.realtime.neutrino.data.latency.LatencyMetric;
import com.observeai.platform.realtime.neutrino.service.newrelic.MetricsCollector;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LatencyProfilerUtil {

	public static void reportLatencyProfilerMetricsToNR(LatencyProfiler profiler, String vendor, String sessionId, String type) {
		int count = profiler.getValuesCount();
		int p100 = profiler.getPercentile(100);
		int p99 = profiler.getPercentile(99);
		int p95 = profiler.getPercentile(95);
		LatencyMetric metric = new LatencyMetric(vendor, sessionId, type, count, p100, p99, p95);
		log.info("Pushing latency metrics {} to New Relic", metric.toMap());
		MetricsCollector.reportMetricsAsEvent("rt_neutrino_session_latency", metric.toMap());
	}

	public static LatencyProfiler getDefaultLatencyProfiler() {
		return new VariableWidthBinHistogramManager();
	}

}
