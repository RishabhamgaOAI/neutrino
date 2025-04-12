package com.observeai.platform.realtime.neutrino.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.commons.data.enums.CallMetadataType;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.neutrino.context.ContextThreadLocal;
import com.observeai.platform.realtime.neutrino.context.MdcFieldNames;
import com.observeai.platform.realtime.neutrino.context.ObserveContext;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.auth.AuthenticationService; 
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import com.observeai.platform.realtime.neutrino.data.dto.AccountAndUserInfoResponseDto;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.data.dto.NiceEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.MetadataBasedProperties;
import com.observeai.platform.realtime.neutrino.data.dto.NiceIntegrationConfig;
import com.observeai.platform.realtime.neutrino.data.nice.CxOneWebSocketMessage;
import com.observeai.platform.realtime.neutrino.data.nice.NiceInitializeMessage;
import com.observeai.platform.realtime.neutrino.data.store.CallRepository;
import com.observeai.platform.realtime.neutrino.enums.CallStatus;
import com.observeai.platform.realtime.neutrino.enums.WsCloseStatus;
import com.observeai.platform.realtime.neutrino.exception.neutrino.AuthenticationFailureException;
import com.observeai.platform.realtime.neutrino.redis.CallEventsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallMetadataRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallStatusRedisStore;
import com.observeai.platform.realtime.neutrino.service.CallHandlingService;
import com.observeai.platform.realtime.neutrino.service.NiceEventHandlerService;
import com.observeai.platform.realtime.neutrino.util.*;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NiceInContactCallHandler extends AbstractWebSocketHandler {

    private final CallEventsRedisStore callEventsRedisStore;
    private final CallStatusRedisStore callStatusRedisStore;
    private final ConcurrentHashMap<WebSocketSession, Call> sessions = new ConcurrentHashMap<>();
    private final CallHandlingService callHandlingService;
    private final NiceEventHandlerService niceEventHandlerService;
    private static final String NICE_VENDOR_NAME = "NICE";
    private static final String AUTHENTICATION_TOKEN_KEY = "authenticationToken";
    private final DravityRequestUtil dravityRequestUtil;
    private static final ObjectMapper objectMapper = ObjectMapperFactory.getPascalCaseObjectMapper();
    private final CallRepository callRepository;
    private final CallMetadataRedisStore callMetadataRedisStore;
    private final AuthenticationService authenticationService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        callHandlingService.handleConnectionEstablishTasks(sessions, session, NICE_VENDOR_NAME);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Call call = sessions.get(session);
        JSONObject jsonMessage = new JSONObject(message.getPayload());
        try {
            if (MessageUtil.isInitializeMessage(jsonMessage)) {
                NiceInitializeMessage initializeMessage = NiceInitializeMessage.fromJsonMessage(jsonMessage);

                String vendorCallId = initializeMessage.getVendorCallId();
                String vendorAccountId = initializeMessage.getVendorAccountId();
                String vendorAgentId = initializeMessage.getAgentId();

                AccountAndUserInfoResponseDto accountAndUserInfo = getAccountInfoAndUserInfo(vendorAccountId, vendorAgentId, vendorCallId);
                if(accountAndUserInfo == null) {
                    log.error("Unable to get accountInfo from dravity. closing current session for callId={}, accountId={}", vendorCallId, vendorAccountId);
                    closeSession(session);
                    return;
                }

                MetadataBasedProperties metadataProps = accountAndUserInfo.getAccountInfo().getMetadataBasedProperties();
                if (metadataProps != null && metadataProps.isMetadataBasedScriptsEnabled()) {
                    callMetadataRedisStore.push(initializeMessage.getVendorCallId(), Constants.VENDOR, Constants.NICE);
                    callMetadataRedisStore.push(initializeMessage.getVendorCallId(), CallMetadataType.START_EVENT_METADATA, initializeMessage);
                }

                Optional<NiceIntegrationConfig> niceConfig = Optional.ofNullable(accountAndUserInfo.getAccountInfo())
                        .map(AccountInfoWithVendorDetailsDto::getVendorAccountDetails)
                        .map(AccountInfoWithVendorDetailsDto.VendorAccountDetailsDto::getConfig)
                        .map(AccountInfoWithVendorDetailsDto.VendorAccountConfig::getNiceConfig);

                if (niceConfig.isEmpty()) {
                    log.warn("nice config is not present for vendorAccountId={}, vendorCallId={}. skipping authentication", vendorAccountId, vendorCallId);
                } else if (!niceConfig.get().isAudioStreamAuthenticationEnabled()) {
                    log.info("audio stream authentication is disabled for vendorAccountId={}, vendorCallId={}. skipping authentication", vendorAccountId, vendorCallId);
                } else {
                    String authToken = jsonMessage.optString(AUTHENTICATION_TOKEN_KEY, null);
                    if (authToken == null || authToken.trim().isEmpty()) {
                        log.error("authentication token is null or empty for vendorCallId={}, vendorAccountId={}", vendorCallId, vendorAccountId);
                        session.close(WsCloseStatus.UNAUTHORIZED.toCloseStatus());
                        return;
                    }

                    try {
                        authenticationService.verifyAuth(authToken, accountAndUserInfo.getAccountInfo().getDeploymentCluster());
                    } catch (AuthenticationFailureException e) {
                        log.error("authentication failed for vendorCallId={}, vendorAccountId={} with error={}", vendorCallId, vendorAccountId, e.toString());
                        session.close(WsCloseStatus.UNAUTHORIZED.toCloseStatus());
                        return;
                    }
                }

                CallStartMessage startMessage = new CallStartMessage(accountAndUserInfo.getAccountInfo(), accountAndUserInfo.getUserMapping().getObserveUserId(), 
                                                                        call.getObserveCallId(), vendorCallId, CallDirection.from(initializeMessage.getDirection()));

                call.setTrack(initializeMessage.getTrack());
                call.setVendor("NICE");
                log.info("NICE: Call Initialize Message received for accountId: {}, agentId: {} having vendorCallId: {}, observeCallId: {}",
                        accountAndUserInfo.getAccountInfo().getObserveAccountId(), accountAndUserInfo.getUserMapping().getObserveUserId(), vendorCallId, call.getObserveCallId());

                Optional<CallEventDto> optionalCallEventDto = callEventsRedisStore.optionalGet(vendorCallId, CallEventType.START_EVENT.name());
                if (optionalCallEventDto.isPresent()) {
                    CallEventDto callEventDto = optionalCallEventDto.get();
                    String firstStreamObserveCallId = callEventDto.getObserveCallId();
                    log.info("NICE: secondary stream detected for accountId: {}, agentId: {}, vendorCallId: {} and updating secondary stream observeCallId: {} to primary stream observeCallId: {}",
                            accountAndUserInfo.getAccountInfo().getObserveAccountId(), accountAndUserInfo.getUserMapping().getObserveUserId(), vendorCallId, call.getObserveCallId(), firstStreamObserveCallId);

                    call.setSecondaryStreamObserveCallId(call.getObserveCallId());
                    callRepository.updateObserveCallId(call, firstStreamObserveCallId);
                    call.setSecondaryStream(true);
                    MDC.put(MdcFieldNames.OBSERVE_CALL_ID.getValue(), call.getObserveCallId());
                    MDC.put(MdcFieldNames.SECONDARY_CALL_ID.getValue(), call.getSecondaryStreamObserveCallId());
                    ObserveContext observeContext = ContextThreadLocal.getObserveContext();
                    if (observeContext != null) {
                        observeContext.setObserveCallId(call.getObserveCallId());
                        observeContext.setSecondaryCallId(call.getSecondaryStreamObserveCallId());
                        ContextThreadLocal.setObserveContext(observeContext);
                    }

                    handleCallResume(call, initializeMessage, accountAndUserInfo);
                }
                callHandlingService.handleConnectionStartTasks(sessions, session, startMessage);
                sendWebSocketResponse(session);
                log.info("NICE: sent stream acknowledgement for vendorCallId: {} and call initiation observeCallId: {}",
                        vendorCallId, call.getSecondaryStreamObserveCallId()!=null ? call.getSecondaryStreamObserveCallId() : call.getObserveCallId());
            }
        } catch(JSONException e) {
            log.error("Unable to process the received text message {}", jsonMessage);
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        Call call = sessions.get(session);
        if(call.getTrack() == null)
            return;

        RawAudioMessage audioMessage = RawAudioMessage.fromBinaryMessage(message, AudioTrack.fromString(call.getTrack()));
        callHandlingService.handleConnectionActivityTasks(sessions, session, audioMessage);
    }

    private void closeSession(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException ex) {
            log.error("sessionId={}, error while closing the websocket session due to error={}", session.getId(), ex.toString(), ex);
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        callHandlingService.handleConnectionCloseTasks(sessions, session, status);
    }

    /* NICE expects the acknowledgement for each websocket initialize message in the below format
       before proceeding with the audio stream transmission */
    private void sendWebSocketResponse(WebSocketSession session) throws IOException {
        CxOneWebSocketMessage response = new CxOneWebSocketMessage("CONNECTED", "COMMAND", "BEGIN AUDIO STREAM");
        String connectedMessage = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(connectedMessage));
    }

    private AccountAndUserInfoResponseDto getAccountInfoAndUserInfo(String vendorAccountId, String vendorAgentId, String callId){
        HttpResponse<AccountAndUserInfoResponseDto> httpResponse = null;
        try {
            httpResponse = dravityRequestUtil.getAccountAndUserInfo(NICE_VENDOR_NAME, vendorAccountId, vendorAgentId);
            if (httpResponse.hasError()) {
                log.error("Error response from dravity for callId: {}, vendorAccountId: {}, vendorAgentId: {} cause = {}", callId, vendorAccountId, vendorAgentId, httpResponse.getError().getCause());
                return null;
            }
        } catch (URISyntaxException e) {
            log.error("URI Syntax error for  callId: {}, vendorAccountId: {}, vendorAgentId: {}", callId, vendorAccountId, vendorAgentId);
            return null;
        }
        return httpResponse.getResponse();
    }

    /** This is a temp sol until we get NICE Resume Event, this sol can cause race condition when hold event come
        late and streams comes early */
    private void handleCallResume(Call call, NiceInitializeMessage initializeMessage, AccountAndUserInfoResponseDto accountAndUserInfo){
        Optional<CallStatus> optionalCallStatus = Optional.ofNullable(callStatusRedisStore.get(call.getObserveCallId()));
        if(optionalCallStatus.isPresent()){
            CallStatus callStatus = optionalCallStatus.get();
            if(CallStatus.HOLD.equals(callStatus)){
                call.setResume(true);
                NiceEventDto niceEventDto = NiceEventDto.builder()
                        .event(Constants.CALL_RESUME)
                        .accountId(initializeMessage.getVendorAccountId())
                        .agentId(initializeMessage.getAgentId())
                        .contactId(initializeMessage.getVendorCallId())
                        .callDirection(initializeMessage.getDirection()).build();
                niceEventHandlerService.handleCallResumeEvent(niceEventDto, accountAndUserInfo);
            }
        }
    }

    public Set<WebSocketSession> getActiveSessions() {
        return sessions.keySet();
    }

    private boolean isSupervisorAssistAudioEnabled(AccountInfoWithVendorDetailsDto accountInfoWithVendorDetailsDto) {
        if (accountInfoWithVendorDetailsDto.getSupervisorAssistProperties() == null)
            return false;
        return accountInfoWithVendorDetailsDto.getSupervisorAssistProperties().getAudioEnabled();
    }

}
