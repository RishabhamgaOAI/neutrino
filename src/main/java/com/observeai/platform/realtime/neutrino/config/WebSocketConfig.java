package com.observeai.platform.realtime.neutrino.config;

import com.observeai.authorization.client.dto.AuthContextDTO;
import com.observeai.platform.realtime.neutrino.auth.AuthenticationService;
import com.observeai.platform.realtime.neutrino.auth.GenesysAuthInterceptor;
import com.observeai.platform.realtime.neutrino.data.common.CallSessionMetadata;
import com.observeai.platform.realtime.neutrino.decorator.MdcWebSocketHandlerDecorator;
import com.observeai.platform.realtime.neutrino.decorator.SessionAwareWebSocketHandlerDecorator;
import com.observeai.platform.realtime.neutrino.exception.neutrino.AuthenticationFailureException;
import com.observeai.platform.realtime.neutrino.exception.neutrino.InternalServerException;
import com.observeai.platform.realtime.neutrino.handler.*;
import com.observeai.platform.realtime.neutrino.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

import static com.observeai.platform.realtime.neutrino.util.Constants.AUTH_CONTEXT;
import static com.observeai.platform.realtime.neutrino.util.Constants.CallSourceNameConstants;

@EnableWebSocket
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {
    public static final String DATA = "data";
    public static final String OBSERVE_CALL_ID = "observe-call-id";
    public static final String RECONNECTION = "reconnection";
    private static final String TOKEN = "token";
    private final CallHandler callHandler;
    private final Five9Handler five9Handler;
    private final CallStreamerHandler callStreamerHandler;
    private final GenesysPureConnectCallHandler genesysPureConnectCallHandler;
    private final GenesysPureCloudCallHandler genesysPureCloudCallHandler;
    private final NiceInContactCallHandler niceInContactCallHandler;
    private final CCCLogicCallHandler cccLogicCallHandler;
    private final AuthenticationService authenticationService;
    private final GenesysAuthInterceptor genesysAuthInterceptor;
    public static final String HEADER_PARAM = "header";
    @Value("${ENABLE_AUTH:false}")
    private boolean enableAuth;

    public boolean isEnableAuth() {
        return enableAuth;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registerAndDecorateHandler(registry, callHandler, new String[]{"/twilio/stream", "/talkdesk/stream"}, authInterceptor(), handshakeInterceptor(), twilioHandshakeInterceptor());
        registerAndDecorateHandler(registry, genesysPureConnectCallHandler, new String[]{"/genesys/stream"}, authInterceptor(), handshakeInterceptor(), genesysHandshakeInterceptor());
        registerAndDecorateHandler(registry, genesysPureCloudCallHandler, new String[]{"/genesys-cloud/stream"}, genesysAuthInterceptor, handshakeInterceptor(), genesysHandshakeInterceptor());
        registerAndDecorateHandler(registry, callHandler, new String[]{"/observe/stream"}, authInterceptor(), handshakeInterceptor(), observeHandshakeInterceptor());
        registerAndDecorateHandler(registry, callStreamerHandler, new String[]{"/observe-monitoring/stream"}, authInterceptor(), handshakeInterceptor(), observeHandshakeInterceptor());
        registerAndDecorateHandler(registry, five9Handler, new String[]{"/five9/stream"}, handshakeInterceptor(), five9HandshakeInterceptor());
        registerAndDecorateHandler(registry, niceInContactCallHandler, new String[]{"/nice/stream"}, handshakeInterceptor(), niceHandshakeInterceptor());
        registerAndDecorateHandler(registry, cccLogicCallHandler, new String[]{"/3clogic/stream"}, handshakeInterceptor(), cccLogicHandshakeInterceptor());
        registerAndDecorateHandler(registry, callHandler, new String[]{"/zoom-cc/stream"}, handshakeInterceptor(), zoomCCHandshakeInterceptor());
        registerAndDecorateHandler(registry, callHandler, new String[]{"/aws-connect/stream"}, handshakeInterceptor(), awsHandshakeInterceptor());
    }

    private void registerAndDecorateHandler(WebSocketHandlerRegistry registry, WebSocketHandler handler, String[] paths, HandshakeInterceptor... interceptors) {
        for (String path : paths) {
            registry.addHandler(decorateHandler(handler), path)
                    .setAllowedOrigins("*")
                    .addInterceptors(interceptors);
        }
    }

    @Bean
    public WebSocketHandlerDecoratorFactory mdcWebSocketHandlerDecoratorFactory() {
        return handler -> new MdcWebSocketHandlerDecorator(handler);
    }

    @Bean
    public WebSocketHandlerDecoratorFactory sessionAwareWebSocketHandlerDecoratorFactory() {
        return handler -> new SessionAwareWebSocketHandlerDecorator(handler);
    }

    private WebSocketHandler decorateHandler(WebSocketHandler handler) {
        return sessionAwareWebSocketHandlerDecoratorFactory().decorate(
                mdcWebSocketHandlerDecoratorFactory().decorate(handler));
    }

    @Bean
    public HandshakeInterceptor authInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                           WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                if (!enableAuth) {
                    return true;
                }
                Map<String, String> queryParams = UrlUtil.readQueryParams(request.getURI());
                String authHeader = queryParams.get(TOKEN);
                AuthContextDTO authContextDTO = null;
                try {
                     authContextDTO = authenticationService.authenticate(authHeader);
                } catch (AuthenticationFailureException | InternalServerException ex) {
                    log.error("Exception while verifying auth. Error: {}, Header: {}", ex.getMessage(), authHeader);
                }

                if (authContextDTO == null || authContextDTO.getAccountId() == null) {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;
                }
                attributes.put(AUTH_CONTEXT, authContextDTO);
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Exception exception) {
            }
        };
    }

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
                                           ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler,
                                           Map<String, Object> attributes) throws Exception {
                List<String> remoteIP = serverHttpRequest.getHeaders().get("X-Forwarded-For");
                Map<String, String> queryParams = UrlUtil.readQueryParams(serverHttpRequest.getURI());
                attributes.put(DATA, new CallSessionMetadata(serverHttpRequest.getURI().toString()));
                if (queryParams.containsKey(OBSERVE_CALL_ID)) {
                    attributes.put(OBSERVE_CALL_ID, queryParams.get(OBSERVE_CALL_ID));
                }
                if (queryParams.containsKey(RECONNECTION)) {
                    attributes.put(RECONNECTION, queryParams.getOrDefault(RECONNECTION, String.valueOf(false)));
                }
                log.info("remote address of partner {}", remoteIP);
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest,
                                       ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
            }
        };
    }

    @Bean
    public HandshakeInterceptor observeHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
                                           ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler,
                                           Map<String, Object> attributes) {
                addCallSourceNameToAttributes(attributes, CallSourceNameConstants.OBSERVE_AUDIO_CAPTURE_CALL_SOURCE_NAME);
                getCallSessionMetadata(attributes).setCallWatchCall(true);
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest,
                                       ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
            }
        };
    }

    @Bean
    public HandshakeInterceptor twilioHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
                                           ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler,
                                           Map<String, Object> attributes) {
                addCallSourceNameToAttributes(attributes, CallSourceNameConstants.TWILIO_CALL_SOURCE_NAME);
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest,
                                       ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
            }
        };
    }

    @Bean
    public HandshakeInterceptor awsHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
                                           ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler,
                                           Map<String, Object> attributes) {
                addCallSourceNameToAttributes(attributes, CallSourceNameConstants.AWS_CONNECT_CALL_SOURCE_NAME);
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest,
                                       ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
            }
        };
    }

    @Bean
    public HandshakeInterceptor five9HandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
                                           ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler,
                                           Map<String, Object> attributes) {
                addCallSourceNameToAttributes(attributes, CallSourceNameConstants.FIVE9_CALL_SOURCE_NAME);
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest,
                                       ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
            }
        };
    }

    @Bean
    public HandshakeInterceptor niceHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
                                           ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler,
                                           Map<String, Object> attributes) {
                addCallSourceNameToAttributes(attributes, CallSourceNameConstants.NICE_CALL_SOURCE_NAME);
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest,
                                       ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
            }
        };
    }

    @Bean
    public HandshakeInterceptor cccLogicHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
                                           ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler,
                                           Map<String, Object> attributes) {
                addCallSourceNameToAttributes(attributes, CallSourceNameConstants.CCCLOGIC_CALL_SOURCE_NAME);
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest,
                                       ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
            }
        };
    }

    @Bean
    public HandshakeInterceptor zoomCCHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
                                           ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler,
                                           Map<String, Object> attributes) {
                addCallSourceNameToAttributes(attributes, CallSourceNameConstants.ZOOMCC_CALL_SOURCE_NAME);
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest,
                                       ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
            }
        };
    }

    @Bean
    public HandshakeInterceptor genesysHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
                                           ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler,
                                           Map<String, Object> attributes) {
                addCallSourceNameToAttributes(attributes, CallSourceNameConstants.GENESYS_CALL_SOURCE_NAME);
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest,
                                       ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
            }
        };
    }


    private void addCallSourceNameToAttributes(Map<String, Object> attributes, String callSourceName) {
        getCallSessionMetadata(attributes).setCallSourceName(callSourceName);
    }

    private CallSessionMetadata getCallSessionMetadata(Map<String, Object> attributes) {
        return (attributes.containsKey(DATA) ? ((CallSessionMetadata) attributes.get(DATA)) : new CallSessionMetadata());
    }


}
