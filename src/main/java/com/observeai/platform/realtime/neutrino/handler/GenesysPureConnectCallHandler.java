package com.observeai.platform.realtime.neutrino.handler;

import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.service.CallHandlingService;
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
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GenesysPureConnectCallHandler extends AbstractWebSocketHandler {
    private static final String COMMAND = "command";
    private static final String OPEN_COMMAND = "open";
    private static final String CLOSE_COMMAND = "close";
    private static final String GENESYS_VENDOR_NAME = "Genesys";

    private final ConcurrentHashMap<WebSocketSession, Call> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, Boolean> isOpenConnAckedBySession = new ConcurrentHashMap<>();
    private final CallHandlingService callHandlingService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        isOpenConnAckedBySession.put(session, false);
        callHandlingService.handleConnectionEstablishTasks(sessions, session, GENESYS_VENDOR_NAME);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Call call = sessions.get(session);
        log.info("observeCallId: {}, text message received on Genesys Pure Connect handler: {}", call.getObserveCallId(), message.getPayload());

        Boolean isOpenConnectionAcked = isOpenConnAckedBySession.get(session);
        if(isOpenConnectionAcked!=null && !isOpenConnectionAcked){
            log.info("observeCallId: {}, acknowledging open connection with Genesys Pure Connect", call.getObserveCallId());
            session.sendMessage(new TextMessage("{\"command\": \"opened\"}"));
            log.info("observeCallId: {}, acknowledged open connection with Genesys Pure Connect", call.getObserveCallId());
            isOpenConnAckedBySession.put(session, true);
        }
//        JSONObject jsonObject = new JSONObject(message.getPayload());
//        String command = jsonObject.getString(COMMAND);
//        if (StringUtils.hasLength(command)) {
//            if (command.equals(OPEN_COMMAND)) {
//                JSONObject parameters = jsonObject.getJSONObject("parameters");
//                String callId = parameters.getLong("interactionId")+"";
//                //TODO get these parameters from genesys
//                CallStartMessage startMessage = CallStartMessage.builder()
//                        .accountId("citrix-genesys")
//                        .agentId(parameters.getString("agentId"))
//                        .recordAudio(false)
//                        .vendorCallId(callId)
//                        .direction(CallDirection.INBOUND)
//                        .build();
//                callHandlingService.handleConnectionStartTasks(sessions, session, startMessage);
//                try {
//                    session.sendMessage(new TextMessage("{\"command\": \"opened\"}"));
//                } catch (IOException e) {
//                    log.error("account_type=genesys failed to send response for open message");
//                }
//            }
//            if (command.equals(CLOSE_COMMAND)) {
//                try {
//                    session.sendMessage(new TextMessage("{\"command\": \"closed\"}"));
//                    session.close();
//                } catch (IOException e) {
//                    log.error("account_type=genesys failed to send response for close message");
//                    session.close();
//                }
//            }
//        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        Call call = sessions.get(session);
        log.info("observeCallId: {}, binary message received on Genesys Pure Connect handler: {}", call.getObserveCallId(), message.getPayload());

//        RawAudioMessage audioMessage = RawAudioMessage.fromBinaryMessage(message, AudioTrack.STEREO);
//        callHandlingService.handleConnectionActivityTasks(sessions, session, audioMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        callHandlingService.handleConnectionCloseTasks(sessions, session, status);
        isOpenConnAckedBySession.remove(session);
    }

}
