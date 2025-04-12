package com.observeai.platform.realtime.neutrino.service.newrelic.latency;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A histogram manager that uses variable width bins to store latency values and calculate percentiles.
 * The bins are defined as follows:
 * 0-249: 1ms width
 * 250-999: 50ms width
 * 1000-4999: 100ms width
 * Percentile values won't be exact, the error will be within the width of the bin. Always reports the upper bound of the bin as the percentile value.
 */
@Getter
public class VariableWidthBinHistogramManager implements LatencyProfiler {
	private static final int MAX_VALUE = 5000;
	private static final List<HistogramBinsSection> sections;

	private int valuesCount;
	private final TreeMap<Integer, Integer> countByBinUpperBound;

	static {
		sections = new ArrayList<>();
		sections.add(new HistogramBinsSection(0, 250, 1));
		sections.add(new HistogramBinsSection(250, 1000, 50));
		sections.add(new HistogramBinsSection(1000, MAX_VALUE + 1, 100));
	}

	public VariableWidthBinHistogramManager() {
		this.valuesCount = 0;
		this.countByBinUpperBound = new TreeMap<>();
	}

	/**
	 * Adds a value to the histogram. Time complexity: O(1)
	 */
	public void addValue(int value) {
		value = Math.min(value, MAX_VALUE);
		for (HistogramBinsSection section : sections) {
			if (value >= section.from && value < section.to) {
				int binUpperBound = (value / section.width) * section.width + section.width - 1;
				countByBinUpperBound.put(binUpperBound, countByBinUpperBound.getOrDefault(binUpperBound, 0) + 1);
				break;
			}
		}
		valuesCount++;
	}

	/**
	 * Returns the percentile value. Time complexity: O(n) where n is the number of bins.
	 */
	public int getPercentile(double percentile) {
		int targetCount = (int) Math.ceil((percentile / 100) * valuesCount);
		int cumulativeCount = 0;

		for (Map.Entry<Integer, Integer> entry : countByBinUpperBound.entrySet()) {
			cumulativeCount += entry.getValue();
			if (cumulativeCount >= targetCount)
				return entry.getKey();
		}
		return countByBinUpperBound.isEmpty() ? 0 : countByBinUpperBound.lastKey(); // fallback
	}

	@AllArgsConstructor
	public static class HistogramBinsSection {
		private final int from;
		private final int to;
		private final int width;
	}
}

