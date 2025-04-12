package com.observeai.platform.realtime.neutrino.service;

import static com.observeai.platform.realtime.neutrino.data.CallState.*;
import com.observeai.platform.realtime.neutrino.client.DeepgramClient;
import com.observeai.platform.realtime.neutrino.client.asr.deepgram.DeepgramClientProvider;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.DeepgramCallState;
import com.observeai.platform.realtime.neutrino.observer.CallStateObserver;
import com.observeai.platform.realtime.neutrino.redis.DeepgramRedisStore;
import com.observeai.platform.realtime.neutrino.redis.RedisValueStore;
import com.observeai.platform.realtime.neutrino.service.newrelic.latency.LatencyProfiler;
import com.observeai.platform.realtime.neutrino.service.newrelic.latency.LatencyProfilerUtil;
import com.observeai.platform.realtime.neutrino.util.CallStateUtil;
import com.observeai.platform.realtime.neutrino.util.DeepgramUtil;
import com.observeai.platform.realtime.neutrino.util.RawAudioMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DeepgramService implements CallStateObserver {
    private final ConcurrentHashMap<String, DeepgramClient> clients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Call, DeepgramCallState> deepgramCallStates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Call, LatencyProfiler> latencyProfilers = new ConcurrentHashMap<>();
    private final DeepgramClientProvider deepgramClientProvider;
    private final DeepgramRedisStore deepgramRedisStore;
    private final RedisValueStore redisValueStore;
    private final CallStateUtil callStateUtil;
    private final DeepgramUtil deepgramUtil;

    @Override
    public void onConnectionEstablished(Call call) {
        log.info("observeCallId={}, connection established on path={}", call.getObserveCallId(), call.getSessionMetadata().getSocketAddress());
    }

    @Override
    public void onMediaMessageReceived(Call call, RawAudioMessage message) {
        if (!call.isProcessMediaMessages())
            return;

        long arrivalTimestamp = System.currentTimeMillis();
        processMediaMessage(call, message);
        int timeTaken = (int) (System.currentTimeMillis() - arrivalTimestamp);
        latencyProfilers.computeIfAbsent(call, k -> LatencyProfilerUtil.getDefaultLatencyProfiler()).addValue(timeTaken);
    }

    private void processMediaMessage(Call call, RawAudioMessage message) {
        DeepgramCallState deepgramCallState = deepgramCallStates.computeIfAbsent(call, k -> initDeepgramCallState(k));
        DeepgramClient client = getDeepgramClient(call, deepgramCallState);
        if (client.isOpen()) {
            client.processAudioMessage(message);
        } else {
	        deepgramCallState.incrementNonProcessedAudioMessages();
        }
        pushKeepAliveInRedis(call);
    }

    private DeepgramClient getDeepgramClient(Call call, DeepgramCallState deepgramCallState) {
        String observeCallId = call.getCallInitiationObserveCallId();
        DeepgramClient client = clients.get(observeCallId);
        if (client != null && !client.isOpen()) {
            log.info("observeCallId={}, deepgram client not in open state. deallocating and returning it back", observeCallId);
            client.deallocate();
            deepgramClientProvider.returnDeepgramClient(call, client);
            client = null;
        }
        if (client == null) {
            log.info("ObserveCallId={}, no active deepgram client found for the call. requesting for a new client", observeCallId);
            client = deepgramClientProvider.requestDeepgramClient(call);
            clients.put(observeCallId, client);
            client.allocate(deepgramCallState);
        }
        return client;
    }

    private boolean shouldProcessMediaMessage(Call call) {
        if (call.isCallStreamerCall()) {
            return call.getState().equals(STARTED) || call.getState().equals(ACTIVE_PROCESSING);
        } else {
            return callStateUtil.isCallInActiveProcessingState(call);
        }
    }

    @Override
    public void onSecondStreamEnded(Call call) {
        persistAndCleanup(call);
    }

    public void persistAndCleanup(Call call) {
        pushDeepgramSeqIdInRedis(call);
        closeAndRemoveDgSession(call);
    }

    @Override
    public void onEnded(Call call) {
        closeAndRemoveDgSession(call);
    }

    private void closeAndRemoveDgSession(Call call) {
        String observeCallId = call.getCallInitiationObserveCallId();
	    if (clients.containsKey(observeCallId)) {
	        clients.get(observeCallId).deallocate();
            deepgramClientProvider.returnDeepgramClient(call, clients.get(observeCallId));
	        clients.remove(observeCallId);
	    }
        DeepgramCallState deepgramCallState = deepgramCallStates.get(call);
        if (deepgramCallState != null) {
            log.info("observeCallId={}, vendorCallId={}, speaker={}, total non processed audio messages={}",
                    call.getObserveCallId(), call.getStartMessage().getVendorCallId(), call.getTrack(), deepgramCallState.getNonProcessedAudioMessages());
        }
        deepgramCallStates.remove(call);

        if (latencyProfilers.containsKey(call)) {
            LatencyProfiler latencyProfiler = latencyProfilers.get(call);
            String key = call.getObserveCallId() + "-" + call.getTrack();
            LatencyProfilerUtil.reportLatencyProfilerMetricsToNR(latencyProfiler, call.getVendor(), key, "dg_processing");
            latencyProfilers.remove(call);
        }
    }

    public long getActiveDeepgramSessionsCount() {
        return clients.values().stream().filter(client -> client != null && client.isOpen()).count();
    }

    private void pushDeepgramSeqIdInRedis(Call call) {
        String observeCallId = call.getCallInitiationObserveCallId();
        if (clients.containsKey(observeCallId) && deepgramCallStates.containsKey(call)) {
            deepgramRedisStore.pushSeqNumber(call, deepgramCallStates.get(call));
        }
    }

    private void pushKeepAliveInRedis(Call call) {
        long currentTimestamp = System.currentTimeMillis();
        Long keepAliveTimeStamp = call.getKeepAliveTimeStamp();
        if (!"FIVE9".equals(call.getVendor()) || (keepAliveTimeStamp != null && (currentTimestamp - keepAliveTimeStamp) < 60000)) {
            return;
        }
        // persist in redis value store with ttl of 30 mins
        String key = call.getObserveCallId() + "-" + "keep-alive-timestamp";
        redisValueStore.push(key, currentTimestamp,  Duration.ofMinutes(30));
        call.setKeepAliveTimeStamp(currentTimestamp);
    }

    private DeepgramCallState initDeepgramCallState(Call call) {
        double timeOffset = deepgramUtil.computeTimeOffset(call);
        long audioMessageSeqNum = deepgramUtil.getSeqNumber(call, "seq_audio");  //TODO: make seq number string to avoid using hack for secondary stream
        long transcriptMessageSeqNum = deepgramUtil.getSeqNumber(call, "seq_transcript"); //TODO: make seq number string to avoid using hack for secondary stream
        DeepgramCallState deepgramCallState =  new DeepgramCallState(call, timeOffset, audioMessageSeqNum, transcriptMessageSeqNum);
        log.info("observeCallId={}, initialized deepgram call state. timeOffset={}, audioMessageSeqNum={}, transcriptMessageSeqNum={}",
                call.getObserveCallId(), deepgramCallState.getTimeOffset(), deepgramCallState.getAudioMessageSeqNum(), deepgramCallState.getTranscriptMessageSeqNum());
        return deepgramCallState;
    }
}
