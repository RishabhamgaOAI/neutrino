package com.observeai.platform.realtime.neutrino.service;

import com.observeai.platform.realtime.neutrino.config.CallSourceConfig;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.dto.CallDetailsUpdateReqDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.util.RawAudioMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

public interface CallHandlingService {
    void handleConnectionEstablishTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, String vendor);

    void handleConnectionStartTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, CallStartMessage callStartMessage, CallSourceConfig mediaFormat);

    void handleConnectionStartTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, CallStartMessage callStartMessage);

    void handleConnectionActivityTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, RawAudioMessage message);

    void handleConnectionCloseTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, CloseStatus closeStatus);

    void handleConnectionUpdateTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, CallDetailsUpdateReqDto req);

    void handleReconnectionStartTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, String observeCallId);

    void handleReconnectionCleanupTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, CloseStatus status);
}
