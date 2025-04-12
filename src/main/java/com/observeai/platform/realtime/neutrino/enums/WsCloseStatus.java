package com.observeai.platform.realtime.neutrino.enums;

import lombok.Getter;
import org.springframework.web.socket.CloseStatus;

@Getter
public enum WsCloseStatus {
	INVALID_RECONNECTION(4009, "Invalid reconnection"),
	MONITORING_TIMEOUT(4010, "Monitoring call timeout"),
	CALL_TIMEOUT(4011, "Call duration timeout"),
	UNAUTHORIZED(3000, "Unauthorized");

	private final int value;
	private final String reasonPhrase;

	WsCloseStatus(int value, String reasonPhrase) {
		this.value = value;
		this.reasonPhrase = reasonPhrase;
	}

	public CloseStatus toCloseStatus() {
		return new CloseStatus(this.value, this.reasonPhrase);
	}
}
