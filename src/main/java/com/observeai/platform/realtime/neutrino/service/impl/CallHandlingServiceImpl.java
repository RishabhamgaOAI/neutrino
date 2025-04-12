package com.observeai.platform.realtime.neutrino.service.impl;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.observeai.platform.realtime.neutrino.config.CallProperties;
import com.observeai.platform.realtime.neutrino.context.ContextThreadLocal;
import com.observeai.platform.realtime.neutrino.config.CallSourceConfig;
import com.observeai.platform.realtime.neutrino.context.MdcFieldNames;
import com.observeai.platform.realtime.neutrino.context.ObserveContext;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallMetadata;
import com.observeai.platform.realtime.neutrino.data.CallMetricsEvent;
import com.observeai.platform.realtime.neutrino.data.CallState;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import com.observeai.platform.realtime.neutrino.data.common.CallSessionMetadata;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoConcise;
import com.observeai.platform.realtime.neutrino.data.dto.CallDetailsUpdateReqDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.data.store.CallRepository;
import com.observeai.platform.realtime.neutrino.enums.WsCloseStatus;
import com.observeai.platform.realtime.neutrino.redis.CallRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallStartMessagesRedisStore;
import com.observeai.platform.realtime.neutrino.redis.LiveCallsRedisStore;
import com.observeai.platform.realtime.neutrino.service.CallHandlingService;
import com.observeai.platform.realtime.neutrino.service.CallSourceConfigService;
import com.observeai.platform.realtime.neutrino.service.CallStateManager;
import com.observeai.platform.realtime.neutrino.service.DeepgramService;
import com.observeai.platform.realtime.neutrino.service.newrelic.CallMetricsCollector;
import com.observeai.platform.realtime.neutrino.util.DravityRequestUtil;
import com.observeai.platform.realtime.neutrino.util.RawAudioMessage;
import com.observeai.platform.realtime.neutrino.util.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.observeai.platform.realtime.neutrino.config.WebSocketConfig.OBSERVE_CALL_ID;
import static com.observeai.platform.realtime.neutrino.util.Constants.CALL_ID_PREFIX;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallHandlingServiceImpl implements CallHandlingService {
    private final CallStateManager callStateManager;
    private final LiveCallsRedisStore liveCallsRedisStore;
    private final CallStartMessagesRedisStore callStartMessageStore;
    private final CallMetricsCollector callMetricsCollector;
    private final CallRedisStore callRedisStore;
    private final CallProperties callProperties;
    private final DeepgramService deepgramService;
    private final CallSourceConfigService callSourceConfigService;
    private final ScheduledExecutorService reconnectionTaskExecutor;
    private final DravityRequestUtil dravityRequestUtil;
    private final CallRepository callRepository;


    @Override
    // @Trace(metricName = "CallHandlingServiceImpl.handleConnectionEstablishTasks()", dispatcher = true)
    public void handleConnectionEstablishTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, String vendor) {
        // addNewRelicCustomParams(sessions, session);
        String observeCallId = CALL_ID_PREFIX + UUID.randomUUID();
        if (session.getAttributes().containsKey(OBSERVE_CALL_ID)) {
            observeCallId = (String)session.getAttributes().get(OBSERVE_CALL_ID);
        }
        MDC.put(MdcFieldNames.OBSERVE_CALL_ID.getValue(), observeCallId);
        ObserveContext observeContext = ContextThreadLocal.getObserveContext();
    
        observeContext.setObserveCallId(observeCallId);
        session.getAttributes().put("observeContext", observeContext);
        ContextThreadLocal.getObserveContext().setObserveCallId(observeCallId);
        
        log.info("assigned {} as observeCallId for conn with sessionId={}", observeCallId, session.getId());
        CallSessionMetadata metadata = WebSocketUtil.getCallSessionMetadata(session);
        Call call = new Call(observeCallId, observeCallId, null, session, metadata, vendor);
        callRepository.addCall(call);
        sessions.put(session, call);
        callStateManager.updateState(call, CallState.CONNECTION_ESTABLISHED);
    }

    @Override
    public void handleConnectionStartTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, CallStartMessage callStartMessage) {
        Call call = handleCallTransferCase(sessions, session);
        if (call == null || call.getState() == CallState.ENDED) {
            return;
        }
        log.info("ObserveCallId: {}, Using default values for callSourceConfig", call.getObserveCallId());
        handleConnectionStartTasks(sessions, session, callStartMessage, null);
    }

    @Override
    // @Trace(metricName = "CallHandlingServiceImpl.handleConnectionStartTasks()", dispatcher = true)
    public void handleConnectionStartTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session,
                                           CallStartMessage callStartMessage, CallSourceConfig mediaFormat) {
        // addNewRelicCustomParams(sessions, session);
        Call call = handleCallTransferCase(sessions, session);
        if (call == null || call.getState() == CallState.ENDED) {
            return;
        }
        callRepository.updateCallStartMessage(call, callStartMessage);
        String callSourceName = WebSocketUtil.getCallSessionMetadata(session).getCallSourceName();
        Optional<CallSourceConfig> callSourceConfig = callSourceConfigService.getCallSourceConfig(callSourceName, mediaFormat);
        if (callSourceConfig.isEmpty()) {
            log.error("ObserveCallId: {}, CallSourceConfig not found for callSourceName: {}", call.getObserveCallId(), callSourceName);
            callStateManager.updateSelfAndAncestorsState(call, CallState.ENDED);
            callRepository.removeCall(call);
            return;
        } else {
            log.info("ObserveCallId: {}, Using callSourceConfig: {}", call.getObserveCallId(), callSourceConfig.get());
            call.setCallSourceConfig(callSourceConfig.get());
        }

        log.info("ObserveCallId: {}, configured callSourceConfig values: {}", call.getObserveCallId(), callSourceConfig);
        if (callStartMessage.isPreviewCall()) {
            log.info("Preview call is running for requestId {} with agentId {} and accountId {} and experienceId {} and callId {}",
                    callStartMessage.getVendorCallId(), callStartMessage.getAgentId(), callStartMessage.getAccountId(),
                    callStartMessage.getExperienceId(), call.getObserveCallId());
            log.info("observeCallId={}, fetching preview call transcription configs", call.getObserveCallId());
            AccountInfoConcise accountInfo = dravityRequestUtil.getAccountInfoByObserveAccountId(callStartMessage.getAccountId()).getResponse();
            call.setPreviewCallsTranscriptionConfigs(accountInfo.getPreviewCallsTranscriptionConfigs());
        }

        if (call.isSecondaryStream()) {
            log.info("start message received for secondary stream callId {}, vendorCallId:{} with track:{}", call.getObserveCallId(), callStartMessage.getVendorCallId(), call.getTrack());
            callStateManager.updateState(call, CallState.SECONDARY_STREAM_STARTED);
            if (!call.isResume())
                callRepository.removeCall(call);
        } else {
            log.info("start message received for callId {}, vendorCallId:{}", call.getObserveCallId(), callStartMessage.getVendorCallId());
            callStateManager.updateState(call, CallState.STARTED);
        }
    }

    @Override
    // @Trace(metricName = "CallHandlingServiceImpl.handleConnectionActivityTasks()", dispatcher = true)
    public void handleConnectionActivityTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, RawAudioMessage message) {
        addNewRelicCustomParams(sessions, session);
        Call call = handleCallTransferCase(sessions, session);
        if (call == null || call.getState() == CallState.ENDED) {
            return;
        }

        if (call.getElapsedTimeInSeconds() > callProperties.getMaxCallDurationInSeconds()) {
            log.error("ObserveCallId: {}, SessionId: {}, call duration exceeds max threshold. closing the session", call.getObserveCallId(), session.getId());
	        try {
		        session.close(WsCloseStatus.CALL_TIMEOUT.toCloseStatus());
	        } catch (IOException e) {
		        log.error("ObserveCallId: {}, Unable to close ws session. Error: ", call.getObserveCallId(), e);
	        }
        }

        if (!call.getStartMessage().isComplete()) {
            if (call.getElapsedTimeInSeconds() >= callProperties.getTimeoutForActiveProcessingInSeconds()) {
                log.info("observeCallId={}, call didnt reach active processing state within timeout."
                    + "ending the call forcefully", call.getObserveCallId());
                try {
                    session.close(new CloseStatus(4001, "Callback meta event timeout"));
                } catch (IOException e) {
                    log.error("ObserveCallId: {}, Unable to close ws session. Error: ", call.getObserveCallId(), e);
                }
                callStateManager.updateSelfAndAncestorsState(call, CallState.ENDED);
                callRepository.removeCall(call);
                trackMissingCallBackMetaEvent(call);
            }
        }
        callStateManager.onMediaMessageReceived(call, message);
    }

    @Override
    // @Trace(metricName = "CallHandlingServiceImpl.handleConnectionCloseTasks()", dispatcher = true)
    public void handleConnectionCloseTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, CloseStatus closeStatus) {
        addNewRelicCustomParams(sessions, session);
        log.info("SessionId: {}, Performing connection close tasks", session.getId());
        if (!sessions.containsKey(session))
            return;
        Call call = sessions.get(session);
        if (Objects.nonNull(closeStatus) && closeStatus.getCode() == CloseStatus.NORMAL.getCode()) {
            log.info("Abnormal close of call-audio session: {} from vendor for call: {}, status: {}",
                    call.getCallAudioSession(), call.getObserveCallId(), closeStatus);
            callStateManager.reportAbnormalCallClose(call, closeStatus);
        }

        if (!updateToEnded(call)) {
            callStateManager.updateSelfAndAncestorsState(call, CallState.SECONDARY_STREAM_ENDED);
        } else {
            callStateManager.updateSelfAndAncestorsState(call, CallState.ENDED);
            callRepository.removeCall(call);
        }
        sessions.remove(session);
    }

    @Override
    // @Trace(metricName = "CallHandlingServiceImpl.handleConnectionUpdateTasks()", dispatcher = true)
    public void handleConnectionUpdateTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, CallDetailsUpdateReqDto req) {
        Call call = sessions.get(session);
        if (call == null || call.getStartMessage() == null) {
            log.error("Call/CallStartMessage is missing. Cannot process manual update");
            return;
        }

        log.info("ObserveCallId: {}, Handling call details update request", call.getObserveCallId());
        if (req.getDirection() != null) {
            CallDirection existing = call.getStartMessage().getDirection();
            if (existing != req.getDirection())
                call.getStartMessage().setDirection(req.getDirection());
            CallMetadata metadata = liveCallsRedisStore.get(call);
            if (metadata != null && metadata.getDirection() != req.getDirection()) {
                metadata.setDirection(req.getDirection());
                liveCallsRedisStore.update(call, metadata);
            }
            log.info("ObserveCallId: {}, Direction updated from {} to {}", call.getObserveCallId(), existing, req.getDirection());
        }
    }

    @Override
    public void handleReconnectionStartTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, String observeCallId) {
        if (observeCallId == null) {
            log.error("SessionId: {}, ObserveCallId is null. Invalid reconnection", session.getId());
            closeSession(session, WsCloseStatus.INVALID_RECONNECTION.toCloseStatus());
        }

        Call call = callRedisStore.getCallForReconnectionAndRemove(observeCallId);
        if (call == null) {
            log.error("SessionId: {}, Call not found in redis. Invalid reconnection", session.getId());
	        closeSession(session, WsCloseStatus.INVALID_RECONNECTION.toCloseStatus());
        }

        call.setCallAudioSession(session);
        log.info("ObserveCallId: {}, SessionId: {}, Reconnection start completed for call", call.getObserveCallId(), session.getId());
        sessions.put(session, call);
        callRepository.addCall(call);
    }

    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException ex) {
            log.error("SessionId: {}, Error while closing the session. Error: ", session.getId(), ex);
        }
    }

    @Override
    public void handleReconnectionCleanupTasks(Map<WebSocketSession, Call> sessions, WebSocketSession session, CloseStatus status) {
        Call call = sessions.get(session);
        if (call == null) {
            log.error("SessionId: {}, Invalid reconnection cleanup. Call not found in sessions", session.getId());
            return;
        }

        try {
            call.setCallAudioSession(null);
            deepgramService.persistAndCleanup(call);
            callRedisStore.persistForReconnection(call);
            waitForReconnectionAndCleanup(sessions, session, status, call);
        } catch (JsonProcessingException th) {
            log.error("SessionId: {}, Error while persisting reconnection cleanup tasks", session.getId(), th);
            handleConnectionCloseTasks(sessions, session, status);
        }
    }

    private void waitForReconnectionAndCleanup(Map<WebSocketSession, Call> sessions, WebSocketSession session, CloseStatus status, Call call) {
        reconnectionTaskExecutor.schedule(() -> {
            try {
                log.info("SessionId: {}, Reconnection cleanup for call: {}", session.getId(), call.getObserveCallId());
                boolean reconnected = !callRedisStore.contains(call.getObserveCallId());
                if (!reconnected) {
                    log.info("SessionId: {}, No reconnection within timeout. Performing connection closed tasks", session.getId());
                    handleConnectionCloseTasks(sessions, session, status);
                } else {
                    log.info("SessionId: {}, Reconnection happened within timeout. Performing reconnection cleanup", session.getId());
                    callRepository.removeCall(call);
                    sessions.remove(session);
                    callRedisStore.delete(call.getObserveCallId());
                }
            } catch (Throwable th) {
                log.error("SessionId: {}, Error while performing reconnection cleanup tasks. Ending gracefully", session.getId(), th);
                handleConnectionCloseTasks(sessions, session, status);
            }
        }, callProperties.getTimeoutForReconnectionInSeconds(), TimeUnit.SECONDS);
    }

    private Call handleCallTransferCase(Map<WebSocketSession, Call> sessions, WebSocketSession session) {
        Call call = sessions.get(session);
        if (Objects.isNull(call)) {
            return null;
        }
    
        if (shouldTransferCall(call)) {
            Call childCall = call.getChildCall();
            sessions.put(session, childCall);
            updateMDCWithObserveCallId(childCall.getObserveCallId());
            updateObserveContextWithCallId(childCall.getObserveCallId());
            return childCall;
        }
    
        return call;
    }
    
    
    private boolean shouldTransferCall(Call call) {
        return call.getState() == CallState.ENDED_FOR_TRANSFER && Objects.nonNull(call.getChildCall());
    }
    
    private void updateMDCWithObserveCallId(String observeCallId) {
        MDC.put(MdcFieldNames.OBSERVE_CALL_ID.getValue(), observeCallId);
    }
    
    private void updateObserveContextWithCallId(String observeCallId) {
        ObserveContext observeContext = ContextThreadLocal.getObserveContext();
        if (observeContext != null) {
            observeContext.setObserveCallId(observeCallId);
            ContextThreadLocal.setObserveContext(observeContext);
        }
    }

    private boolean updateToEnded(Call call) {
        //In case of non NICE & Five9 vendor, always update to call end state
        if(!("NICE".equals(call.getVendor()) || "FIVE9".equals(call.getVendor()))) return true;

        if ("FIVE9".equals(call.getVendor()) && call.getState().equals(CallState.CONNECTION_ESTABLISHED)) return true;

        if("FIVE9".equals(call.getVendor()) && !call.getStartMessage().isReconnectionAllowed()) return !call.isSecondaryStream();

        return handleSplitStreamCallEndCases(call);
    }

    private boolean handleSplitStreamCallEndCases(Call call){
        //In case of non resumed secondary stream, update to secondary state ended state
        if(call.isSecondaryStream() && !call.isResume()) return false;

        //In case of incomplete start message, update to call end state
        if(call.getStartMessage().getVendorCallId() == null || !call.getStartMessage().isComplete()){
            log.info("update state to call end as vendorCallId or startMessage is incomplete for observeCallId {}", call.getObserveCallId());
            return true;
        }

        return false;
    }

    private void trackMissingCallBackMetaEvent(Call call) {
        CallMetricsEvent event = new CallMetricsEvent(call.getObserveCallId(), null, null, null,
            call.getVendor(), null, null, null, call.getStartTime(), "MISSING_CALL_BACK_START_EVENT");
        callMetricsCollector.reportCallMetricsEvent(event);
    }

    private void addNewRelicCustomParams(Map<WebSocketSession, Call> sessions, WebSocketSession session) {
        NewRelic.addCustomParameter("sessionId", session.getId());
        if (sessions.containsKey(session))
            NewRelic.addCustomParameter("observeCallId", sessions.get(session).getObserveCallId());
    }
}
