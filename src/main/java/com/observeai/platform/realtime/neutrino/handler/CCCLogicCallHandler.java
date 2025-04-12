package com.observeai.platform.realtime.neutrino.handler;

import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.ccclogic.StartMessage;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import com.observeai.platform.realtime.neutrino.data.dto.AccountAndUserInfoResponseDto;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoConcise;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.service.CallHandlingService;
import com.observeai.platform.realtime.neutrino.util.DravityRequestUtil;
import com.observeai.platform.realtime.neutrino.util.MessageUtil;
import com.observeai.platform.realtime.neutrino.util.RawAudioMessage;
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
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CCCLogicCallHandler extends AbstractWebSocketHandler {

    private final ConcurrentHashMap<WebSocketSession, Call> sessions = new ConcurrentHashMap<>();
    private final CallHandlingService callHandlingService;
    private final DravityRequestUtil dravityRequestUtil;

    private static final String CCCLOGIC_VENDOR_NAME = "3CLOGIC";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        callHandlingService.handleConnectionEstablishTasks(sessions, session, CCCLOGIC_VENDOR_NAME);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        JSONObject jsonMessage = new JSONObject(message.getPayload());
        try {
            if (MessageUtil.isStartMessage(jsonMessage)) {
                String observeCallId = sessions.get(session).getObserveCallId();
                StartMessage startMessage = StartMessage.fromJsonMessage(jsonMessage);

                String vendorAccountId = startMessage.getVendorAccountId();
                String agentId = startMessage.getAgentId();
                String vendorCallId = startMessage.getVendorCallId();

                AccountAndUserInfoResponseDto dravityResponse;
                try {
                    dravityResponse = getAccountAndUserInfo(vendorAccountId, agentId);
                }
                catch(URISyntaxException e) {
                    log.error("Dravity API exception. Unable to fetch account details for accountId: " + vendorAccountId, e.getMessage());
                    session.close();
                    return;
                }

                CallStartMessage callStartMessage = new CallStartMessage(dravityResponse.getAccountInfo(), observeCallId, vendorCallId, CallDirection.from(startMessage.getDirection()));
                callHandlingService.handleConnectionStartTasks(sessions, session, callStartMessage);
            } else if (MessageUtil.isMediaMessage(jsonMessage)) {
                RawAudioMessage audioMessage = RawAudioMessage.fromJsonMessage(jsonMessage);
                callHandlingService.handleConnectionActivityTasks(sessions, session, audioMessage);
            }
        } catch(JSONException e) {
            log.error("Unable to process the received text message {} exception: {}", jsonMessage, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        callHandlingService.handleConnectionCloseTasks(sessions, session, status);
    }

    public AccountAndUserInfoResponseDto getAccountAndUserInfo(String accountId, String agentId) throws URISyntaxException{
        return dravityRequestUtil.getAccountAndUserInfo(CCCLOGIC_VENDOR_NAME, accountId, agentId).getResponse();
    }

    public Set<WebSocketSession> getActiveSessions() {
        return sessions.keySet();
    }

    private boolean isSupervisorAssistAudioEnabled(AccountInfoWithVendorDetailsDto accountInfoWithVendorDetailsDto) {
        if(accountInfoWithVendorDetailsDto.getSupervisorAssistProperties() == null)
            return false;
        return accountInfoWithVendorDetailsDto.getSupervisorAssistProperties().getAudioEnabled();
    }

}
