package com.observeai.platform.realtime.neutrino.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.commons.data.enums.TimestampEvent;
import com.observeai.platform.realtime.commons.data.messages.CallAudioMessage;
import com.observeai.platform.realtime.commons.data.messages.DeepgramTranscriptResponse;
import com.observeai.platform.realtime.commons.data.messages.details.AudioMessage;
import com.observeai.platform.realtime.commons.data.messages.details.DeepgramMessage;
import com.observeai.platform.realtime.commons.data.messages.details.TimestampMessage;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.DeepgramCallState;
import com.observeai.platform.realtime.neutrino.data.deepgram.DeepgramClientKey;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.util.*;
import jakarta.websocket.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;

@Slf4j
@ClientEndpoint
public class DeepgramClient extends Endpoint {
    @Getter
    private String id;
    @Getter
    private boolean partOfPool;
    private Session wsConnectionSession;
    private final DeepgramClientKey deepgramClientKey;
    private final KafkaProducer producer;
    private final KafkaProperties kafkaProperties;
    private final SlackClient slackClient;
    private final DeepgramProperties deepgramProperties;
    private final DeepgramUtil deepgramUtil;
    private final ObjectMapper mapper;
    private DeepgramCallState deepgramCallState;
    private double previousCallOffsetToRemove;

    public DeepgramClient(String id, boolean partOfPool, DeepgramClientKey deepgramClientKey, KafkaProducer producer, KafkaProperties kafkaProperties, SlackClient slackClient, DeepgramProperties deepgramProperties, DeepgramUtil deepgramUtil) {
        this.id = id;
        this.partOfPool = partOfPool;
        this.deepgramClientKey = deepgramClientKey;
        this.producer = producer;
        this.kafkaProperties = kafkaProperties;
        this.slackClient = slackClient;
        this.deepgramProperties = deepgramProperties;
        this.deepgramUtil = deepgramUtil;
        this.mapper = ObjectMapperFactory.getSnakeCaseObjectMapper();
        this.connect();
    }

    public synchronized void connect() {
        if (wsConnectionSession != null && wsConnectionSession.isOpen()) {
            log.info("deepgramClientId={}, deepgramWsSessionId={}, connection already established. no need for reconnection", id, getWsConnectionSessionId());
            return;
        }
        String authHeader = deepgramClientKey.isOnPrem() ?  Constants.BASIC_AUTH_PREFIX + deepgramProperties.getOnPremSecretKey() : Constants.TOKEN_AUTH_PREFIX + deepgramProperties.getSecretKey();
        String host = deepgramClientKey.isOnPrem() ?  deepgramProperties.getOnPremHost(): deepgramProperties.getHost();
        try {
            log.info("deepgramClientId={}, establishing connection to host={}", id, host);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                    .configurator(new DeepgramClient.AuthorizationConfigurator(authHeader))
                    .build();
            URI deepgramUri = UriComponentsBuilder.newInstance()
                    .scheme(deepgramProperties.getWsScheme())
                    .host(host)
                    .port(deepgramProperties.getPort())
                    .path(deepgramProperties.getRealtimePath())
                    .queryParam("encoding", deepgramClientKey.getCallSourceConfig().getEncoding())
                    .queryParam("sample_rate", deepgramClientKey.getCallSourceConfig().getSampleRate())
                    .queryParam("channels", deepgramClientKey.getCallSourceConfig().getChannels())
                    .queryParam("multichannel", deepgramClientKey.getCallSourceConfig().getChannels() > 1)
                    .queryParam("model", deepgramProperties.getModelName())
                    .queryParam("interim_results", deepgramProperties.isInterimResults())
                    .build().toUri();
            this.wsConnectionSession = container.connectToServer(this, config, deepgramUri);
            log.info("deepgramClientId={}, deepgramWsSessionId={}, established connection to uri={}", id, getWsConnectionSessionId(), deepgramUri.toString());
            this.previousCallOffsetToRemove = 0;
        } catch (Exception e) {
            log.error("deepgramClientId={}, failed to establish connection to host={} due to error={}", id, host, e.toString(), e);
        }
    }

    public boolean isOpen() {
        return wsConnectionSession != null && wsConnectionSession.isOpen();
    }

	public String getWsConnectionSessionId() {
        return wsConnectionSession == null ? null : wsConnectionSession.getId();
    }

    public boolean sendKeepAliveMessage() {
        return sendTextMessage("{\"type\":\"KeepAlive\"}");
    }

    public boolean sendTextMessage(String text) {
        if (!isOpen()) {
            log.error("deepgramClientId={}, unable to send message as deepgram connection is not open. message={}", id, text);
            return false;
        }

        synchronized (wsConnectionSession) {
            wsConnectionSession.getAsyncRemote().sendText(text);
        }
        return true;
    }

    public boolean sendBinaryMessage(ByteBuffer byteBuffer) {
        if (!isOpen()) {
            log.error("deepgramClientId={}, unable to send binary message as deepgram connection is not open", id);
            return false;
        }

        synchronized (wsConnectionSession) {
            wsConnectionSession.getAsyncRemote().sendBinary(byteBuffer);
        }
        return true;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        log.info("deepgramClientId={}, deepgramWsSessionId={}, connection established to DG, sessionId={}", id,  getWsConnectionSessionId(), session.getId());
        session.addMessageHandler(String.class, message -> onMessage(session, message));
    }

    public void allocate(DeepgramCallState deepgramCallState) {
        log.info("deepgramClientId={}, deepgramWsSessionId={}, allocated for observeCallId={} initiationObserveCallId={}", id, getWsConnectionSessionId(),
                deepgramCallState.getCall().getObserveCallId(), deepgramCallState.getCall().getCallInitiationObserveCallId());
        this.deepgramCallState = deepgramCallState;
        this.previousCallOffsetToRemove = -1;
    }

    public void deallocate() {
        log.info("deepgramClientId={}, deepgramWsSessionId={}, deallocated for observeCallId={} initiationObserveCallId={}", id, getWsConnectionSessionId(),
                deepgramCallState.getCall().getObserveCallId(), deepgramCallState.getCall().getCallInitiationObserveCallId());
        if (isOpen()) {
            log.info("deepgramClientId={}, deepgramWsSessionId={}, sent finalize message to DG", id, getWsConnectionSessionId());
            this.wsConnectionSession.getAsyncRemote().sendText("{\"type\":\"Finalize\"}");
        }
        this.deepgramCallState = null;
        this.previousCallOffsetToRemove = -1;
    }

    public void onMessage(Session session, String message) {
        try {
            DeepgramMessage deepgramMessage = deserialize(session, message);
            if (deepgramMessage == null || deepgramMessage.getTransactionKey() != null)
                return;

            long arrivalTimestamp = System.currentTimeMillis();
            if (acceptDgMessage(deepgramMessage, arrivalTimestamp)) {
                if (previousCallOffsetToRemove == -1) {
                    previousCallOffsetToRemove = deepgramMessage.getStart();
                    deepgramCallState.setTimeOffset(deepgramCallState.getTimeOffset() - previousCallOffsetToRemove);
                    log.info("deepgramClientId={}, deepgramWsSessionId={}, received first message for observeCallId={}, start={}, duration={}, timeoffset={}",
                            id, getWsConnectionSessionId(), deepgramCallState.getCall().getObserveCallId(), deepgramMessage.getStart(), deepgramMessage.getDuration(), deepgramCallState.getTimeOffset());
                }

                Call call = deepgramCallState.getCall();
                deepgramUtil.updateDgMessage(deepgramMessage, deepgramCallState);
                long seqNum = deepgramCallState.transcriptMessageSeqNum++;
                DeepgramTranscriptResponse deepgramTranscriptResponse = DeepgramTranscriptResponse.fromDeepgramMessage(
                        call.getObserveCallId(), call.getStartMessage().getAccountId(), call.getStartMessage().getAgentId(),
                        seqNum, TimestampUtil.getCurrentTimeMillis(), deepgramMessage);
                deepgramCallState.registerCurrentDgMessage(deepgramMessage.getSpeaker(), arrivalTimestamp);
                producer.produceMessage(kafkaProperties.getTopics().getCallMessageTopic(), call.getObserveCallId(), deepgramTranscriptResponse);
                if (kafkaProperties.isPushToLatencyTopic())
                    producer.produceMessage(kafkaProperties.getTopics().getLatencyCallMessageTopic(), call.getObserveCallId(), deepgramTranscriptResponse);
            }
        } catch (NullPointerException ex) {
            log.error("deepgramClientId={}, deepgramWsSessionId={}, error while processing message due to {}", id, getWsConnectionSessionId(), ex.toString());
        } catch (Throwable th) {
            // Even though catching throwable is not recommended, we are catching it here to avoid disconnection of websocket connection
            log.error("deepgramClientId={}, deepgramWsSessionId={}, error while processing message due to {}", id, getWsConnectionSessionId(), th.toString(), th);
        }
    }

    private boolean acceptDgMessage(DeepgramMessage dgResponse, Long arrivalTimestamp) {
        if (dgResponse.isFinal())
            return true;
        return deepgramCallState.getRecentDgMessageTimestamp(dgResponse.getSpeaker()).map(timestamp -> {
            long delay = arrivalTimestamp - timestamp;
            return (delay >= deepgramProperties.getCutoffToAcceptNonFinalInMillis());
        }).orElse(false);
    }

    @Override
    public void onClose(Session session, CloseReason reason) {
        log.info("deepgramClientId={}, deepgramWsConnectionId={}, connection closed from DG. code={}, reason={}", id, getWsConnectionSessionId(), reason.getCloseCode(), reason.getReasonPhrase());
    }

    public void close() {
        log.info("deepgramClientId={}, deepgramWsConnectionId={}, closing deepgram connection", id, getWsConnectionSessionId());
        try {
            if (isOpen()) {
                wsConnectionSession.close();
            } else {
                log.info("deepgramClientId={}, unable to close the deepgram connection as it is not open", id);
            }
        } catch (IOException e) {
            log.error("deepgramClientId={}, error while closing deepgram connection due to {}", id, e.toString(), e);
        }
    }

    @Override
    public void onError(Session session, Throwable th) {
        log.error("deepgramClientId={}, deepgramWsConnectionId: {}, error in deepgram connection. error={}", id, getWsConnectionSessionId(), th.toString(), th);
    }

    private void trimBuffers(boolean b2, boolean b3, ByteArrayOutputStream inboundBuffer,
                             ByteArrayOutputStream outboundBuffer) throws IOException {
        if (b2 && b3) {
            byte[] b = inboundBuffer.toByteArray();
            int diff = inboundBuffer.size() - outboundBuffer.size();
            byte[] copyOfRange = Arrays.copyOfRange(b, diff, b.length);
            inboundBuffer.reset();
            inboundBuffer.write(copyOfRange);
        }
    }

    private void trimBufferFromPrefix(ByteArrayOutputStream buffer, int size) throws IOException {
        byte[] b = buffer.toByteArray();
        byte[] copyOfRange = Arrays.copyOfRange(b, size, b.length);
        buffer.reset();
        buffer.write(copyOfRange);
    }

    public void processAudioMessage(RawAudioMessage message) throws IllegalStateException {
        Call call = deepgramCallState.getCall();
        ByteArrayOutputStream inboundBuffer = deepgramCallState.getInboundBuffer();
        ByteArrayOutputStream outboundBuffer = deepgramCallState.getOutboundBuffer();
        boolean emptyByteReceived = false;
        byte[] stereoDataToWrite = null;
        try {
            if (message.getTrack().equals(AudioTrack.STEREO) || call.getCallSourceConfig().isSplitStream()) {
                //TODO buffer_size for stereo data
                stereoDataToWrite = message.getAudioData();
                sendBinaryMessage(ByteBuffer.wrap(message.getAudioData()));
            } else {
                if (message.getTrack().equals(AudioTrack.INBOUND)) {
                    inboundBuffer.write(message.getAudioData());
                } else {
                    outboundBuffer.write(message.getAudioData());
                }
                if (message.getAudioData().length == 0) {
                    emptyByteReceived = true;
                    log.debug("deepgramClientId={}, observeCallId={}, empty byte received", id, call.getObserveCallId());
                }

                int BUFFER_SIZE = call.getCallSourceConfig().getBufferSize();
                if (inboundBuffer.size() >= BUFFER_SIZE && outboundBuffer.size() >= BUFFER_SIZE
                        || emptyByteReceived) {

                    byte[] ib = inboundBuffer.toByteArray();
                    byte[] iBuffer = Arrays.copyOfRange(ib, 0, BUFFER_SIZE);
                    byte[] ob = outboundBuffer.toByteArray();
                    byte[] oBuffer = Arrays.copyOfRange(ob, 0, BUFFER_SIZE);
                    byte[] stereoData = AudioUtil.fromMonoToMultichannel(call.getCallSourceConfig().getBytesPerSample(), iBuffer, oBuffer);

                    stereoDataToWrite = stereoData;
                    sendBinaryMessage(ByteBuffer.wrap(stereoData));
                    inboundBuffer.reset();
                    outboundBuffer.reset();
                    inboundBuffer.write(Arrays.copyOfRange(ib, BUFFER_SIZE, ib.length));
                    outboundBuffer.write(Arrays.copyOfRange(ob, BUFFER_SIZE, ob.length));
                }
            }
            if (pushAudioToKafka(call)) {
                sendAudioData(call, stereoDataToWrite, message.getTrack(), message.getTimestamp());
            }
        } catch (Exception e) {
            log.error("deepgramClientId={}, observeCallId={}, error occurred while processing audio message in " +
                    "deepgram client. error={}", id, call.getObserveCallId(), e.toString(), e);
        }
    }

    private void sendAudioData(Call call, byte[] stereoDataToWrite, AudioTrack track, long timestamp) {
        if (stereoDataToWrite != null) {
            CallAudioMessage callAudioMessage = CallAudioMessage.builder()
                    .callId(call.getObserveCallId())
                    .accountId(call.getStartMessage().getAccountId())
                    .userId(call.getStartMessage().getAgentId())
                    .audioMessage(new AudioMessage(stereoDataToWrite, track, call))
                    .timestampMessage(
                        TimestampMessage.builder()
                            .eventTimestamp(timestamp)
                            .sequenceNum(deepgramCallState.audioMessageSeqNum++)
                            .eventName(TimestampEvent.CALL_AUDIO_PACKET)
                            .build())
                    .build();

            this.producer.produceMessage(kafkaProperties.getTopics().getCallAudioMessageTopic(), call.getObserveCallId(), callAudioMessage);
            if (kafkaProperties.isPushToLatencyTopic())
                this.producer.produceMessage(kafkaProperties.getTopics().getLatencyCallMessageTopic(), call.getObserveCallId(), callAudioMessage);
        }
    }

    private DeepgramMessage deserialize(Session session, String message) {
        try {
            return mapper.readValue(message, DeepgramMessage.class);
        } catch (IOException ex) {
            log.error("deepgramClientId={}, Error while serializing deepgram message for sessionId={}, message={} due to {}", id, session.getId(), message, ex.toString(), ex);
            return null;
        }
    }

    @AllArgsConstructor
    public static class AuthorizationConfigurator extends ClientEndpointConfig.Configurator {
        private final String authHeader;
        private static final String AUTHORIZATION = "Authorization";

        @Override
        public void beforeRequest(Map<String, List<String>> headers) {
            headers.put(AUTHORIZATION, Collections.singletonList(authHeader));
        }
    }

    private boolean pushAudioToKafka(Call call) {
        return call.getStartMessage().isRecordAudio() || call.getStartMessage().isSupervisorAssistAudioEnabled();
    }
}
