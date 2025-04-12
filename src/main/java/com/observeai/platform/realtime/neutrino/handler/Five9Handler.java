package com.observeai.platform.realtime.neutrino.handler;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.observeai.platform.realtime.neutrino.context.ContextThreadLocal;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.neutrino.context.MdcFieldNames;
import com.observeai.platform.realtime.neutrino.context.ObserveContext;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallState;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.data.latency.Five9LatencyProfilerStore;
import com.observeai.platform.realtime.neutrino.data.store.CallRepository;
import com.observeai.platform.realtime.neutrino.enums.Speaker;
import com.observeai.platform.realtime.neutrino.redis.CallEventsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallStartMessagesRedisStore;
import com.observeai.platform.realtime.neutrino.redis.RedisValueStore;
import com.observeai.platform.realtime.neutrino.service.CallHandlingService;
import com.observeai.platform.realtime.neutrino.util.Constants;
import com.observeai.platform.realtime.neutrino.util.MessageUtil;
import com.observeai.platform.realtime.neutrino.util.RawAudioMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.MDC;
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

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Five9Handler extends AbstractWebSocketHandler {

    private final ConcurrentHashMap<WebSocketSession, Call> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, Five9LatencyProfilerStore> latencyProfilerStores = new ConcurrentHashMap<>();
    private final CallHandlingService callHandlingService;
    private final CallStartMessagesRedisStore callStartMessagesRedisStore;
    private final CallEventsRedisStore callEventsRedisStore;
    private final RedisValueStore redisValueStore;
    private final CallRepository callRepository;

    @Override
    // @Trace(metricName = "Five9Handler.afterConnectionEstablished()", dispatcher = true)
    public void afterConnectionEstablished(WebSocketSession session) {
        addNewRelicCustomParams(session);
        callHandlingService.handleConnectionEstablishTasks(sessions, session, "FIVE9");
        latencyProfilerStores.put(session, new Five9LatencyProfilerStore(session.getId()));
    }

    @Override
    // @Trace(metricName = "Five9Handler.handleTextMessage()", dispatcher = true)
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        addNewRelicCustomParams(session);
        JSONObject jsonMessage = new JSONObject(message.getPayload());
        String event = jsonMessage.getString(MessageUtil.EVENT_KEY);
        long arrivalTs = System.currentTimeMillis();
        long gatewayTs = jsonMessage.optLong(MessageUtil.GATEWAY_TS_KEY, arrivalTs);
        processMessage(session, event, jsonMessage);
        long processedTs = System.currentTimeMillis();
        if (latencyProfilerStores.containsKey(session)) {
            latencyProfilerStores.get(session).getGatewayLatencyProfiler().addValue((int) (arrivalTs - gatewayTs));
            latencyProfilerStores.get(session).getProcessingLatencyProfiler().addValue((int) (processedTs - arrivalTs));
        }
    }

    private void processMessage(WebSocketSession session, String event, JSONObject jsonMessage) {
        try {
            Call call = sessions.get(session);
            switch (event) {
                case MessageUtil.START_EVENT:
                    handleStartEvent(session, jsonMessage, call);
                    break;
                case MessageUtil.MEDIA_EVENT:
                    handleMediaEvent(session, jsonMessage, call);
                    break;
                default:
                    log.error("SessionId: {}, Unknown event type found. Event: {}", session.getId(), event);
            }
        } catch(JSONException e) {
            log.error("Unable to process the received text message {}", jsonMessage);
        }
    }

    @Override
    // @Trace(metricName = "Five9Handler.afterConnectionClosed()", dispatcher = true)
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        addNewRelicCustomParams(session);
        callHandlingService.handleConnectionCloseTasks(sessions, session, status);
        if (latencyProfilerStores.containsKey(session)) {
            latencyProfilerStores.get(session).reportMetrics();
            log.info("SessionId: {}, latency profiler metrics reported", session.getId());
            latencyProfilerStores.remove(session);
        } else {
            log.error("SessionId: {}, latency profiler store not found", session.getId());
        }
    }

    private void handleMediaEvent(WebSocketSession session, JSONObject jsonMessage, Call call) {
        if (call.getState().getValue() < CallState.STARTED.getValue() ||
                (call.isSecondaryStream() && call.getObserveCallId().equals(call.getSecondaryStreamObserveCallId())))
            return;
        RawAudioMessage audioMessage = RawAudioMessage.fromJsonMessage(jsonMessage);
        callHandlingService.handleConnectionActivityTasks(sessions, session, audioMessage);
    }

    private void handleStartEvent(WebSocketSession session, JSONObject jsonMessage, Call call){
        JSONObject start = jsonMessage.getJSONObject(MessageUtil.START_EVENT);
        CallStartMessage callStartMessage = CallStartMessage.fromJsonMessage(start, call.getObserveCallId());
        if (isCallEndMetaEventPresent(session, callStartMessage)) {
            log.info("observeCallId={}, sessionId={}, five9 call already ended for vendorCallId={}, agentId={}. closing the session",
                    sessions.get(session).getObserveCallId(), session.getId(), callStartMessage.getVendorCallId(), callStartMessage.getAgentId());
            closeSession(session, CloseStatus.NORMAL);
            return;
        }
        String track = (Speaker.CUSTOMER.equals(callStartMessage.getSpeaker()) ? Constants.INBOUND : Constants.OUTBOUND);
        call.setTrack(track);
        call.setVendor("FIVE9");
        log.info("five9: call start event received for speaker={}, accountId={}, agentId={}, vendorCallId={}, observeCallId={}",
                callStartMessage.getSpeaker(), callStartMessage.getAccountId(), callStartMessage.getAgentId(), callStartMessage.getVendorCallId(), call.getObserveCallId());

        Optional<CallEventDto> optionalCallEventDto = Optional.empty();
        if (callStartMessage.isReconnectionAllowed()) {
            log.info("five9: checking for any existing callEventDto for the stream as reconnection is allowed, " +
                            "speaker={}, accountId={}, agentId={}, vendorCallId={} ,observeCallId={}", callStartMessage.getSpeaker(),
                    callStartMessage.getAccountId(), callStartMessage.getAgentId(), callStartMessage.getVendorCallId(), call.getObserveCallId());
            optionalCallEventDto = callEventsRedisStore.optionalGet(callStartMessage.getVendorCallId(), CallEventType.START_EVENT.name());
        }

        if (optionalCallEventDto.isPresent()) {
            CallEventDto callEventDto = optionalCallEventDto.get();
            String firstStreamObserveCallId = callEventDto.getObserveCallId();
            log.info("five9: secondary stream detected(via redis) for speaker={}, accountId={}, agentId={}, vendorCallId={} " +
                            "and updating secondary stream observeCallId={} to primary stream observeCallId={}",
                    callStartMessage.getSpeaker(), callStartMessage.getAccountId(), callStartMessage.getAgentId(),
                    callStartMessage.getVendorCallId(), call.getObserveCallId(), firstStreamObserveCallId);

            call.setSecondaryStreamObserveCallId(call.getObserveCallId());
            callRepository.updateObserveCallId(call, firstStreamObserveCallId);
            call.setSecondaryStream(true);

            MDC.put(MdcFieldNames.OBSERVE_CALL_ID.getValue(), call.getObserveCallId());
            MDC.put(MdcFieldNames.SECONDARY_CALL_ID.getValue(), call.getSecondaryStreamObserveCallId());
            ObserveContext observeContext = ContextThreadLocal.getObserveContext();
            if (observeContext != null) {
                observeContext.setObserveCallId(call.getObserveCallId());
                observeContext.setSecondaryCallId( call.getSecondaryStreamObserveCallId());
                ContextThreadLocal.setObserveContext(observeContext);
            }
            if(Speaker.AGENT.equals(callStartMessage.getSpeaker())){
                call.setResume(true);
            }
        } else if(Speaker.CUSTOMER.equals(callStartMessage.getSpeaker())){
            log.info("Five9: secondary stream detected(without redis) for customer, accountId: {}, agentId: {}, vendorCallId: {}, observeCallId: {}, and just updating secondary stream observeCallId to current stream observeCallId",
                    callStartMessage.getAccountId(), callStartMessage.getAgentId(), callStartMessage.getVendorCallId(), call.getObserveCallId());
            call.setSecondaryStreamObserveCallId(call.getObserveCallId());
            MDC.put(MdcFieldNames.SECONDARY_CALL_ID.getValue(), call.getSecondaryStreamObserveCallId());
            ObserveContext observeContext = ContextThreadLocal.getObserveContext();
            if (observeContext != null) {
                observeContext.setSecondaryCallId(call.getObserveCallId());
                ContextThreadLocal.setObserveContext(observeContext);
            }
            call.setSecondaryStream(true);
        }

        if (call.isSecondaryStream()) {
            callStartMessagesRedisStore.optionalGet(callStartMessage.getVendorCallId()).filter(CallStartMessage::isComplete)
                    .ifPresentOrElse((callStartMessageFromRedis) ->
                                    callHandlingService.handleConnectionStartTasks(sessions, session, callStartMessageFromRedis),
                            () -> callHandlingService.handleConnectionStartTasks(sessions, session, callStartMessage));
        } else {
            callHandlingService.handleConnectionStartTasks(sessions, session, callStartMessage);
        }
    }

    private static void closeSession(WebSocketSession session, CloseStatus closeStatus) {
        try {
            session.close(closeStatus);
        } catch (IOException e) {
            log.error("SessionId: {}, Unable to close the session due to {}", session.getId(), e.getMessage(), e);
        }
    }

    public Set<WebSocketSession> getActiveSessions() {
        return sessions.keySet();
    }

    private void addNewRelicCustomParams(WebSocketSession session) {
        NewRelic.addCustomParameter("sessionId", session.getId());
        if (sessions.containsKey(session))
            NewRelic.addCustomParameter("observeCallId", sessions.get(session).getObserveCallId());
    }

    private boolean isCallEndMetaEventPresent(WebSocketSession session, CallStartMessage callStartMessage) {
        String vendorCallId = callStartMessage.getVendorCallId();
        String observeAgentId = callStartMessage.getAgentId();
        String key = vendorCallId + "-" + observeAgentId + "-" + CallEventType.END_EVENT.name();

        return redisValueStore.get(key, CallBackMetaEventDto.class).isPresent();
    }
}
