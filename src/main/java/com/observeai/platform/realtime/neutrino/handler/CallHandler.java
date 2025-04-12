package com.observeai.platform.realtime.neutrino.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.config.CallSourceConfig;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.PongMessage;
import com.observeai.platform.realtime.neutrino.data.dto.CallDetailsUpdateReqDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.data.latency.DefaultCallLatencyProfilerStore;
import com.observeai.platform.realtime.neutrino.service.CallHandlingService;
import com.observeai.platform.realtime.neutrino.service.CallSourceConfigService;
import com.observeai.platform.realtime.neutrino.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.observeai.platform.realtime.neutrino.util.MessageUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallHandler extends AbstractWebSocketHandler {
    protected final ConcurrentHashMap<WebSocketSession, Call> sessions = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<WebSocketSession, DefaultCallLatencyProfilerStore> latencyProfilerStores = new ConcurrentHashMap<>();
    protected final CallHandlingService callHandlingService;
    protected final ObjectMapper snakeCaseMapper = ObjectMapperFactory.getSnakeCaseObjectMapper();
    protected final static String PING_RTT_KEY = "rtt";
    protected final static String PARAMETERS_KEY = "parameters";
    protected final DravityRequestUtil dravityRequestUtil;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        callHandlingService.handleConnectionEstablishTasks(sessions, session, null);
        latencyProfilerStores.put(session, new DefaultCallLatencyProfilerStore(session.getId()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // TODO: Use POJO in place of JSONObject
        long arrivalTs = System.currentTimeMillis();
        processMessage(session, message);
        long processedTs = System.currentTimeMillis();
        int processingTime = (int) (processedTs - arrivalTs);
        if (latencyProfilerStores.containsKey(session)) {
            latencyProfilerStores.get(session).getProcessingLatencyProfiler().addValue(processingTime);
        }
    }

    protected void processMessage(WebSocketSession session, TextMessage message) {
        JSONObject jsonMessage = new JSONObject(message.getPayload());
        try {
            if (PING_EVENT.equals(jsonMessage.optString(TYPE_KEY))) {
                JSONObject parameters = jsonMessage.optJSONObject(PARAMETERS_KEY);
                if (parameters != null && parameters.has(PING_RTT_KEY) && latencyProfilerStores.containsKey(session)) {
                    int pingRtt = (int) parameters.getLong(PING_RTT_KEY);
                    latencyProfilerStores.get(session).getPingLatencyProfiler().addValue(pingRtt);
                }
                sendPong(session, jsonMessage.optInt(SEQ_ID, -1));
                return;
            }
            String event = jsonMessage.getString(MessageUtil.EVENT_KEY);
            switch (event) {
                case MessageUtil.START_EVENT:
                    JSONObject start = jsonMessage.optJSONObject(Constants.START_MESSAGE);
                    JSONObject media = Optional.ofNullable(start).map(s -> s.optJSONObject(Constants.MEDIA_FORMAT)).orElse(null);
                    CallStartMessage callStartMessage = CallStartMessage.fromJsonMessage(start, sessions.get(session).getObserveCallId());
                    CallSourceConfig mediaFormat = CallSourceConfigService.parse(media);
                    callHandlingService.handleConnectionStartTasks(sessions, session, callStartMessage, mediaFormat);
                    break;
                case MessageUtil.MEDIA_EVENT:
                    RawAudioMessage audioMessage = RawAudioMessage.fromJsonMessage(jsonMessage);
                    callHandlingService.handleConnectionActivityTasks(sessions, session, audioMessage);
                    break;
                case MessageUtil.UPDATE_EVENT:
                    CallDetailsUpdateReqDto req = MessageUtil.getCallDetailsUpdateReqDto(jsonMessage);
                    callHandlingService.handleConnectionUpdateTasks(sessions, session, req);
                    break;
                default:
                    log.error("SessionId: {}, Unknown event type found. Event: {}", session.getId(), event);
            }
        } catch(IOException | JSONException e) {
            log.error("SessionId: {}, Unable to process the received text message {}", session.getId(), jsonMessage, e);
        }
    }

    void sendPong(WebSocketSession session, int pingId) throws IOException {
        PongMessage pongMessage = new PongMessage(pingId);
        String message = snakeCaseMapper.writeValueAsString(pongMessage);
        session.sendMessage(new TextMessage(message));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String vendor = Optional.ofNullable(sessions.get(session)).map(Call::getVendor).orElse("UNKNOWN");
        callHandlingService.handleConnectionCloseTasks(sessions, session, status);
        if (latencyProfilerStores.containsKey(session)) {
            latencyProfilerStores.get(session).reportMetrics(vendor);
            log.info("SessionId: {}, latency profiler metrics reported", session.getId());
            latencyProfilerStores.remove(session);
        } else {
            log.error("SessionId: {}, latency profiler store not found", session.getId());
        }
    }

    protected void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException ex) {
            log.error("SessionId: {}, Error while closing the session. Error: ", session.getId(), ex);
        }
    }

    public Set<WebSocketSession> getActiveSessions() {
        return sessions.keySet();
    }

}
