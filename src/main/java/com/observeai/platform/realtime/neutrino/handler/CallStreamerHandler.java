package com.observeai.platform.realtime.neutrino.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.config.CallProperties;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.callStreamer.CallStreamerSocketMessage;
import com.observeai.platform.realtime.neutrino.data.callStreamer.SocketMessage;
import com.observeai.platform.realtime.neutrino.data.dto.frontend.CallStreamerStartAckDto;
import com.observeai.platform.realtime.neutrino.enums.WsCloseStatus;
import com.observeai.platform.realtime.neutrino.redis.CallRedisStore;
import com.observeai.platform.realtime.neutrino.service.CallHandlingService;
import com.observeai.platform.realtime.neutrino.service.CallStateManager;
import com.observeai.platform.realtime.neutrino.util.*;
import com.observeai.platform.realtime.neutrino.util.Constants.CallStreamerConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Optional;

import static com.observeai.platform.realtime.neutrino.data.CallState.ACTIVE_PROCESSING;
import static com.observeai.platform.realtime.neutrino.data.CallState.STARTED;

@Slf4j
@Component
public class CallStreamerHandler extends CallHandler {
	private final CallProperties callProperties;
	private final CallStateManager callStateManager;

	public CallStreamerHandler(CallHandlingService callHandlingService, DravityRequestUtil dravityRequestUtil, CallProperties callProperties, CallStateManager callStateManager) {
		super(callHandlingService, dravityRequestUtil);
		this.callProperties = callProperties;
		this.callStateManager = callStateManager;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		if (WebSocketUtil.isReconnection(session)) {
			log.info("SessionId: {}, Received reconnection connection request", session.getId());
			String observeCallId = WebSocketUtil.getObserveCallId(session);
			callHandlingService.handleReconnectionStartTasks(sessions, session, observeCallId);
			return;
		}
		super.afterConnectionEstablished(session);
		sessions.get(session).setCallStreamerCall(true);
	}

	@Override
	protected void processMessage(WebSocketSession session, TextMessage text) {
		try {
			checkSessionTimeout(session);
			SocketMessage message = CallStreamerUtil.deserialize(text.getPayload());
			switch (message.getEvent()) {
				case CallStreamerConstants.MANUAL_CALL_START:
					Call call = sessions.get(session);
					if (STARTED.equals(call.getState())) {
						call.setStartTime(System.currentTimeMillis());
						callStateManager.updateState(sessions.get(session), ACTIVE_PROCESSING);
					}
					CallStreamerUtil.reportEventToNR((CallStreamerSocketMessage) message, getCallId(session));
					break;
				case CallStreamerConstants.MANUAL_CALL_END:
					callHandlingService.handleConnectionCloseTasks(sessions, session, CloseStatus.NORMAL);
					closeSession(session, CloseStatus.NORMAL);
					CallStreamerUtil.reportEventToNR((CallStreamerSocketMessage) message, getCallId(session));
					break;
				case MessageUtil.START_EVENT:
					super.processMessage(session, text);
					sendStartAck(session);
					break;
				default:
					super.processMessage(session, text);
			}
		} catch (IOException e) {
			log.error("SessionId: {}, Error while parsing/processing message. Error: ", session.getId(), e);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		log.info("SessionId: {}, Connection closed with status: {}", session.getId(), status.getCode());
		Call call = sessions.get(session);
		if (call == null) {
			log.error("SessionId: {}, No call found for the session. Closing the session", session.getId());
			sessions.remove(session);
			return;
		}

		if (isEligibleForReconnection(call, status)) {
			log.info("SessionId: {}, Eligible for reconnection. invoking reconnection cleanup tasks", session.getId());
			callHandlingService.handleReconnectionCleanupTasks(sessions, session, status);
		} else {
			super.afterConnectionClosed(session, status);
		}
	}

	private void sendStartAck(WebSocketSession session) throws IOException {
		Call call = sessions.get(session);
		CallStreamerStartAckDto callStreamerStartAckDto = getCallStreamerStartAckDto(call);
		String ack = snakeCaseMapper.writeValueAsString(callStreamerStartAckDto);
		session.sendMessage(new TextMessage(ack));
	}

	private CallStreamerStartAckDto getCallStreamerStartAckDto(Call call) {
		return new CallStreamerStartAckDto(call.getObserveCallId());
	}

	private void checkSessionTimeout(WebSocketSession session) {
		Call call = sessions.get(session);
		if (call == null)
			return;

		if (STARTED.equals(call.getState()) && call.getElapsedTimeInSeconds() > callProperties.getTimeoutForMonitoringInSeconds()) {
			log.error("ObserveCallId: {}, SessionId: {}, monitoring call reached max timeout. closing the session", call.getObserveCallId(), session.getId());
			closeSession(session, WsCloseStatus.MONITORING_TIMEOUT.toCloseStatus());
		}
	}

	private String getCallId(WebSocketSession session) {
		return Optional.ofNullable(sessions.get(session)).map(Call::getObserveCallId).orElse(null);
	}

	private boolean isEligibleForReconnection(Call call, CloseStatus closeStatus) {
		return ACTIVE_PROCESSING.equals(call.getState()) &&
				ListUtil.emptyIfNull(callProperties.getEligibleReconnectionErrorCodes()).contains(closeStatus.getCode());
	}

}
