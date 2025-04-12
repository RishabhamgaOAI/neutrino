package com.observeai.platform.realtime.neutrino.util;

import com.observeai.platform.realtime.neutrino.client.DeepgramClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@Getter
public class WebSocketActiveSession {

    private final Map<WebSocketSession, SessionMetadata> sessions;

    public WebSocketActiveSession() {
        this.sessions = new ConcurrentHashMap<>();
    }

    public void addSession(WebSocketSession session, SessionMetadata sessionMetadata) {
        sessions.put(session, sessionMetadata);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }


    public SessionMetadata getSessionMetadata(WebSocketSession session) {
        return sessions.get(session);
    }

    public DeepgramClient getDeepgramClient(WebSocketSession session) {
        if (sessions.containsKey(session)) {
            return sessions.get(session).getDeepgramClient();
        }
        log.error("failed to get deepgram client");
        return null;
    }


}
