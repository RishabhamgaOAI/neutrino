package com.observeai.platform.realtime.neutrino.service;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallState;
import com.observeai.platform.realtime.neutrino.data.store.CallRepository;
import com.observeai.platform.realtime.neutrino.observer.CallStateObserver;
import com.observeai.platform.realtime.neutrino.service.events.CallStartEventsJoiner;
import com.observeai.platform.realtime.neutrino.util.RawAudioMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallStateManager {
    private final CallRepository callRepository;
    private final List<CallStateObserver> observers;
    private final ConcurrentHashMap<Integer, Integer> abnormalCallCloseCounts = new ConcurrentHashMap<>();
    private final CallStartEventsJoiner callStartEventsJoiner;

    // @Trace(metricName = "CallStateManager.updateState()")
    public void updateState(Call call, CallState toState) {
        synchronized (call.getObserveCallId()) {
            if (!isValidStateTransition(call.getState(), toState)) {
                log.error("observeCallId={}, unable to switch state from {} to {}", call.getObserveCallId(), call.getState(), toState);
                return;
            }

            if (toState.getValue() == CallState.ACTIVE_PROCESSING.getValue() && call.isSecondaryStream()) {
                log.error("observeCallId={}, cannot move secondary stream to ACTIVE_PROCESSING", call.getObserveCallId());
                return;
            }

            long start = System.currentTimeMillis();
            log.info("observeCallId={}, updating the state to {}", call.getObserveCallId(), toState);

            for (CallStateObserver observer : observers) {
                try {
                    observer.execute(call, toState);
                } catch (Throwable th) {
                    NewRelic.noticeError(th);
                    log.error("observeCallId={}, failed to execute observer: {} during state update from {} to {}",
                            call.getObserveCallId(), observer.name(), call.getState(), toState, th);
                    if (observer.isCriticalForProcessing() &&
                            (toState == CallState.STARTED || toState == CallState.ACTIVE_PROCESSING)) {
                        try {
                            log.info("observeCallId={}, critical call state update failure ending ws session={}", call.getObserveCallId(), call.getCallAudioSession());
                            call.getCallAudioSession().close(new CloseStatus(4000, "Failed to process call data"));
                        } catch (IOException e) {
                            log.error("Failed to close ws session with id={} and observeCallId={}", call.getCallAudioSession().getId(), call.getObserveCallId());
                        }
                        return;
                    }
                }
            }

            log.info("ObserveCallId: {}, Updated the state from {} to {} in {} ms", call.getObserveCallId(),
                        call.getState(), toState, System.currentTimeMillis() - start);
            call._updateState(toState);
        }

        // special case for 'STARTED' state to move to next state
        if (CallState.STARTED.equals(call.getState())) {
            if (!call.getStartMessage().isComplete()) {
                log.info("observeCallId={}, start message is not complete. attempting to join.", call.getObserveCallId());
                callStartEventsJoiner.joinAndBroadcast(call.getStartMessage().getVendorCallId()).ifPresent((startMessage) -> {
                    callRepository.updateCallStartMessage(call, startMessage);
                });
            }

            // if start message is complete, switch the state to ACTIVE_PROCESSING and start processing media messages
            if (call.getStartMessage().isComplete() && !call.isCallStreamerCall()) {
                log.info("observeCallId={}, start message is complete.", call.getObserveCallId());
                if (call.isPrimaryStream()) {
                    updateState(call, CallState.ACTIVE_PROCESSING);
                }
                log.info("observeCallId={}, set processMediaMessages to true", call.getObserveCallId());
                call.allowMediaMessageProcessing();
            } else if (!call.getStartMessage().isComplete()){
                log.info("observeCallId={}, start message is not complete. cannot switch state", call.getObserveCallId());
            }
        }
    }

    public void updateSelfAndAncestorsState(Call call, CallState toState) {
        if (call == null) {
            return;
        }

        try {
            updateState(call, toState);
        } catch (Throwable th) {
            log.warn("Failed to update self state for call: {} from {} to {}, attempting state update of ancestors",
                    call.getObserveCallId(), call.getState(), toState, th);
        }
        updateSelfAndAncestorsState(call.getParentCall(), toState);
    }

    // @Trace(metricName = "CallStateManager.onMediaMessageReceived()")
    public void onMediaMessageReceived(Call call, RawAudioMessage message) {
        for (CallStateObserver observer : observers) {
            observer.onMediaMessageReceived(call, message);
        }
    }

    public boolean isValidStateTransition(CallState fromState, CallState toState) {
        return fromState.getValue() < toState.getValue();
    }

    public void reportAbnormalCallClose(Call call, CloseStatus closeStatus) {
        abnormalCallCloseCounts.compute(closeStatus.getCode(), (statusCode, count) -> {
            if (count == null) {
                return 1;
            }
            return ++count;
        });
    }

    public Map<Integer, Integer> getAndRefreshAbnormalCallCloseCounts() {
        Map<Integer, Integer> abnormalCountsMap = abnormalCallCloseCounts.entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        abnormalCallCloseCounts.clear();
        return abnormalCountsMap;
    }
}
