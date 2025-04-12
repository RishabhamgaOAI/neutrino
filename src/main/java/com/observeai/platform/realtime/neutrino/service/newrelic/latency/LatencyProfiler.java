package com.observeai.platform.realtime.neutrino.service.newrelic.latency;

public interface LatencyProfiler {
	void addValue(int value);
	int getPercentile(double percentile);
	int getValuesCount();
}
