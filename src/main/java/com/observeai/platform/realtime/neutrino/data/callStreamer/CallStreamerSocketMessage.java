package com.observeai.platform.realtime.neutrino.data.callStreamer;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@ToString
public class CallStreamerSocketMessage extends SocketMessage {
	private String observeAccountId;
	private String observeUserId;
}
