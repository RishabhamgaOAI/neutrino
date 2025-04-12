package com.observeai.platform.realtime.neutrino.data;

import com.observeai.platform.realtime.neutrino.util.MessageUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PongMessage extends SocketMessage {
	private int id;

	public PongMessage(int id) {
		super(MessageUtil.PONG_EVENT, MessageUtil.PONG_EVENT);
		this.id = id;
	}
}
