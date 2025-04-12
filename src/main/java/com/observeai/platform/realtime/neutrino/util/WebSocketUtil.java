package com.observeai.platform.realtime.neutrino.util;

import com.observeai.platform.realtime.neutrino.config.WebSocketConfig;
import com.observeai.platform.realtime.neutrino.data.common.CallSessionMetadata;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

public class WebSocketUtil {

    public static CallSessionMetadata getCallSessionMetadata(WebSocketSession session) {
        return (CallSessionMetadata) session.getAttributes().get(WebSocketConfig.DATA);
    }

    public static boolean isReconnection(WebSocketSession session) {
        String val = (String) session.getAttributes().get(WebSocketConfig.RECONNECTION);
        return val != null && val.equals("true");
    }

    public static String getObserveCallId(WebSocketSession session) {
        return Optional.ofNullable(session.getAttributes().get(WebSocketConfig.OBSERVE_CALL_ID))
                .map(Object::toString)
                .orElse(null);
    }
}
