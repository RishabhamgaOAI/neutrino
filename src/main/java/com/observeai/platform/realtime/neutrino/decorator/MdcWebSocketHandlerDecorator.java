package com.observeai.platform.realtime.neutrino.decorator;
import com.observeai.platform.realtime.neutrino.context.ContextThreadLocal;
import com.observeai.platform.realtime.neutrino.context.MdcFieldNames;
import com.observeai.platform.realtime.neutrino.context.ObserveContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

@Slf4j
public class MdcWebSocketHandlerDecorator extends WebSocketHandlerDecorator {

    public MdcWebSocketHandlerDecorator(WebSocketHandler delegate) {
        super(delegate);
    }

    @Override 
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            MDC.clear();
            ContextThreadLocal.removeObserveContext();
            ObserveContext observeContext = new ObserveContext();

            String sessionId = session.getId();
            MDC.put(MdcFieldNames.SESSION_ID.getValue(), sessionId);
            observeContext.setSessionId(sessionId);
            ContextThreadLocal.setObserveContext(observeContext);
            super.afterConnectionEstablished(session);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            ObserveContext observeContext = (ObserveContext) session.getAttributes().get("observeContext");
            if (observeContext != null) {
                MDC.put(MdcFieldNames.OBSERVE_CALL_ID.getValue(), observeContext.getObserveCallId());
                MDC.put(MdcFieldNames.SESSION_ID.getValue(), observeContext.getSessionId());
                MDC.put(MdcFieldNames.SECONDARY_CALL_ID.getValue(), observeContext.getSecondaryCallId());
            }
            ContextThreadLocal.setObserveContext(observeContext);
            super.handleMessage(session, message);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try{
            ObserveContext observeContext = (ObserveContext) session.getAttributes().get("observeContext");
            if (observeContext != null) {
                MDC.put(MdcFieldNames.OBSERVE_CALL_ID.getValue(), observeContext.getObserveCallId());
                MDC.put(MdcFieldNames.SESSION_ID.getValue(), observeContext.getSessionId());
                MDC.put(MdcFieldNames.SECONDARY_CALL_ID.getValue(), observeContext.getSecondaryCallId());
            }
            ContextThreadLocal.setObserveContext(observeContext);
            super.afterConnectionClosed(session, status);
        } finally {
            MDC.clear();
        }
    }

}