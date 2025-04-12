package com.observeai.platform.realtime.neutrino.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.data.genesys.ClientMessageBase;
import com.observeai.platform.realtime.neutrino.data.genesys.ServerMessageBase;
import com.observeai.platform.realtime.neutrino.data.latency.GenesysLatencyProfilerStore;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.service.CallHandlingService;
import com.observeai.platform.realtime.neutrino.util.AudioTrack;
import com.observeai.platform.realtime.neutrino.util.DravityRequestUtil;
import com.observeai.platform.realtime.neutrino.util.ObjectMapperFactory;
import com.observeai.platform.realtime.neutrino.util.RawAudioMessage;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import jakarta.websocket.OnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GenesysPureCloudCallHandler extends AbstractWebSocketHandler {
    private static final String ERROR = "error";
    private static final String OPEN = "open";
    private static final String CLOSE = "close";
    private static final String PING = "ping";
    private static final String OPENED = "opened";
    private static final String CLOSED = "closed";
    private static final String PONG = "pong";
    private static final String GENESYS_VENDOR_NAME = "Genesys";
    private static final ObjectMapper objectMapper = ObjectMapperFactory.getPascalCaseObjectMapper();

    private final ConcurrentHashMap<WebSocketSession, Call> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, AtomicLong> serverSeqMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, GenesysLatencyProfilerStore> latencyProfilerStores = new ConcurrentHashMap<>();
    private final CallHandlingService callHandlingService;
    private final DravityRequestUtil dravityRequestUtil;
    private final KafkaProducer producer;
    private final KafkaProperties kafkaProperties;

    @Override
    // @Trace(metricName = "GenesysPureCloudCallHandler.afterConnectionEstablished()", dispatcher = true)
    public void afterConnectionEstablished(WebSocketSession session) {
        // addNewRelicCustomParams(session);
        callHandlingService.handleConnectionEstablishTasks(sessions, session, GENESYS_VENDOR_NAME);
        serverSeqMap.put(session, new AtomicLong(1));
        latencyProfilerStores.put(session, new GenesysLatencyProfilerStore(session.getId()));
    }

    @Override
    // @Trace(metricName = "GenesysPureCloudCallHandler.handleTextMessage()", dispatcher = true)
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // addNewRelicCustomParams(session);
        long arrivalTs = System.currentTimeMillis();
        processMessage(session, message);
        long processedTs = System.currentTimeMillis();
        if (latencyProfilerStores.containsKey(session))
            latencyProfilerStores.get(session).getProcessingLatencyProfiler().addValue((int) (processedTs - arrivalTs));
    }

    private void processMessage(WebSocketSession session, TextMessage message) {
        try {
            ClientMessageBase clientMessage = objectMapper.readValue(message.getPayload(), ClientMessageBase.class);
            Map<String, Object> parameters = clientMessage.getParameters();
            switch (clientMessage.getType()) {
                case ERROR:
                    log.error("error received: {} for sessionId: {}", parameters.get("message"), session.getId());
                    break;
                case OPEN:
                    String observeCallId = sessions.get(session).getObserveCallId();
                    String callId = (String)parameters.get("conversationId");
                    String accountId = (String)parameters.get("organizationId");
                    log.info("genesys open message received for vendorCallId: {}, accountId: {} having sessionId: {}", callId, accountId, session.getId());

                    AccountInfoWithVendorDetailsDto accountInfo = getAccountInfo(accountId, callId);
                    if(accountInfo == null) {
                        log.error("closing current session due to not able to get accountAndUserInfo from dravity for callId={}, accountId={}", callId, accountId);
                        closeCurrentSession(session, clientMessage);
                        break;
                    }
                    CallStartMessage startMessage = new CallStartMessage(accountInfo, observeCallId, callId, CallDirection.INBOUND);
                    callHandlingService.handleConnectionStartTasks(sessions, session, startMessage);
                    ServerMessageBase opened = new ServerMessageBase(clientMessage.getId(), OPENED, serverSeqMap.get(session).getAndIncrement(), getOpenedParams(parameters), clientMessage.getSeq());
                    String openedMessage = objectMapper.writeValueAsString(opened);
                    session.sendMessage(new TextMessage(openedMessage));
                    break;
                case CLOSE:
                    log.info("genesys close message received with message: {}, for sessionId: {}", message.getPayload(), session.getId());
                    closeCurrentSession(session, clientMessage);
                    break;
                case PING:
                    boolean shouldLogForThisSession = shouldLogForThisSession(session.getId());
                    if(shouldLogForThisSession) log.info("genesys ping message received with message: {}, for sessionId: {}", message.getPayload(), session.getId());

                    if (sessions.get(session) != null){
                        if(shouldLogForThisSession) log.info("sending PONG message on PING message: {}, for sessionId: {}", message.getPayload(), session.getId());
                        ServerMessageBase pong = new ServerMessageBase(clientMessage.getId(), PONG, serverSeqMap.get(session).getAndIncrement(), new HashMap<>(), clientMessage.getSeq());
                        String pongMessage = objectMapper.writeValueAsString(pong);
                        session.sendMessage(new TextMessage(pongMessage));
                        if(shouldLogForThisSession) log.info("PONG message sent on PING message: {}, for sessionId: {}", message.getPayload(), session.getId());
                    }
                    else {
                        log.error("not able to send PONG message on PING message: {}, for sessionId: {}, as session is not present", message.getPayload(), session.getId());
                    }
                    String rtt = (String)parameters.getOrDefault("rtt", "PT0S");
                    String secondsPart = rtt.substring(2, rtt.length() - 1);
                    int millis = (int) (Double.parseDouble(secondsPart) * 1000);
                    if (latencyProfilerStores.containsKey(session))
                        latencyProfilerStores.get(session).getPingLatencyProfiler().addValue(millis);
                    break;
                default:
                    log.error("genesys unimplemented message type: {}, message: {}, sessionId: {}", clientMessage.getType(), message.getPayload(), session.getId());
                    break;
            }
        }
        catch(Exception e){
            log.error("error in genesys call handler text message handling: {}, for sessionId: {}", message.getPayload(), session.getId(), e);
        }
    }

    @Override
    // @Trace(metricName = "GenesysPureCloudCallHandler.handleBinaryMessage()", dispatcher = true)
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        addNewRelicCustomParams(session);
        long arrivalTs = System.currentTimeMillis();
        RawAudioMessage audioMessage = RawAudioMessage.fromBinaryMessage(message, AudioTrack.STEREO);
        callHandlingService.handleConnectionActivityTasks(sessions, session, audioMessage);
        long processedTs = System.currentTimeMillis();
        latencyProfilerStores.get(session).getProcessingLatencyProfiler().addValue((int) (processedTs - arrivalTs));
    }

    @OnError
    public void handleError(WebSocketSession session, Throwable throwable) {
        log.error("error occurred in session with sessionId: {} and logged by OnError, errorMessage: {}", session.getId(), throwable.getMessage(), throwable);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable throwable) {
        log.error("error occurred in session with sessionId: {} and logged by handleTransportError, errorMessage: {}", session.getId(), throwable.getMessage(), throwable);
    }

    @Override
    // @Trace(metricName = "GenesysPureCloudCallHandler.afterConnectionClosed()", dispatcher = true)
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        addNewRelicCustomParams(session);
        log.info("starting connection close task for sessionId: {}", session.getId());
        callHandlingService.handleConnectionCloseTasks(sessions, session, status);
        serverSeqMap.remove(session);
        if (latencyProfilerStores.containsKey(session)) {
            latencyProfilerStores.get(session).reportMetrics();
            log.info("SessionId: {}, latency profiler metrics reported", session.getId());
            latencyProfilerStores.remove(session);
        } else {
            log.error("SessionId: {}, latency profiler store not found", session.getId());
        }
    }

    private void closeCurrentSession(WebSocketSession session, ClientMessageBase clientMessage) throws IOException {
        log.info("closing current session with sessionId: {}", session.getId());
        ServerMessageBase closed = new ServerMessageBase(clientMessage.getId(), CLOSED, serverSeqMap.get(session).getAndIncrement(), new HashMap<>(), clientMessage.getSeq());
        String closeMessage = objectMapper.writeValueAsString(closed);
        log.info("sending close message to genesys, message:{}, sessionId: {}", closeMessage, session.getId());
        session.sendMessage(new TextMessage(closeMessage));
        log.info("sent close message to genesys, message:{}, sessionId: {}", closeMessage, session.getId());
        session.close();
        log.info("closed current session with sessionId: {}", session.getId());
    }

    private Map<String, Object> getOpenedParams(Map<String, Object> parameters){
        Map<String, Object> params = new HashMap<>();
        params.put("startPaused", false);

        List<Map<String, Object>> mediaMessages = (List<Map<String, Object>>) parameters.get("media");
        for(Map<String, Object> media : mediaMessages){
            if(((ArrayList) media.get("channels")).size() == 2){
                params.put("media",  Collections.singletonList(media));
                break;
            }
        }

        return params;
    }

    private AccountInfoWithVendorDetailsDto getAccountInfo(String vendorAccountId, String callId){
        HttpResponse<AccountInfoWithVendorDetailsDto> httpResponse = null;
        try {
            httpResponse = dravityRequestUtil.getAccountInfo(GENESYS_VENDOR_NAME, vendorAccountId);
            if (httpResponse == null || httpResponse.hasError()) {
                log.error("getting error response from dravity for callId = {}, vendorAccountId = {}", callId, vendorAccountId, httpResponse.getError().getCause());
                return null;
            }
        } catch (URISyntaxException e) {
            log.error("error in URISyntax of dravity for callId = {}, vendorAccountId = {}", callId, vendorAccountId, e);
            return null;
        }

        return httpResponse.getResponse();
    }

    private boolean shouldLogForThisSession(String sessionId) {
        int hash = sessionId.hashCode();
        int absHash = Math.abs(hash);
        return absHash % 10 <= 2;
    }

    public Set<WebSocketSession> getActiveSessions() {
        return sessions.keySet();
    }

    private void addNewRelicCustomParams(WebSocketSession session) {
        NewRelic.addCustomParameter("sessionId", session.getId());
        if (sessions.containsKey(session))
            NewRelic.addCustomParameter("observeCallId", sessions.get(session).getObserveCallId());
    }
}
