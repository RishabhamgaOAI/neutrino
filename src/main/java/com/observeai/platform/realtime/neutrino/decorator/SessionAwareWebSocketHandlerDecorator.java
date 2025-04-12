package com.observeai.platform.realtime.neutrino.decorator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

@Slf4j
public class SessionAwareWebSocketHandlerDecorator extends WebSocketHandlerDecorator {

	public SessionAwareWebSocketHandlerDecorator(WebSocketHandler delegate) {
		super(delegate);
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		if (!session.isOpen()) {
			log.warn("sessionId={} session is not in open state. ignoring message", session.getId());
			return;
		}
		super.handleMessage(session, message);
	}
}
