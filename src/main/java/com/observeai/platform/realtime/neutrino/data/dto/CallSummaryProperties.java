package com.observeai.platform.realtime.neutrino.data.dto;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CallSummaryProperties {
	private PushSummaryType pushSummaryType = PushSummaryType.NONE;

	public enum PushSummaryType {
		NONE,
		PARAGON
	}
}
