package com.observeai.platform.realtime.commons.data.messages;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@ToString
public class CallStreamerEvent {
	private String observeAccountId;
	private String observeUserId;
	private String observeCallId;
	private String event;
	private String source;
}
