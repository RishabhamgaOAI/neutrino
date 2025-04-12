package com.observeai.platform.realtime.neutrino.observer;

import com.newrelic.api.agent.Trace;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallState;
import com.observeai.platform.realtime.neutrino.util.RawAudioMessage;


public interface CallStateObserver {
    default String name() {
        return this.getClass().getName();
    }
    default boolean isCriticalForProcessing() {
        return false;
    }
    default void onConnectionEstablished(Call call) {}
    default void onStarted(Call call) {}
    default void onSecondStreamStarted(Call call){}
    default void onActiveProcessing(Call call) {}
    default void onEnded(Call call) {}
    default void onEndedForTransfer(Call call) {
        onEnded(call);
    }
    default void onSecondStreamEnded(Call call){}
    default void onMediaMessageReceived(Call call, RawAudioMessage message) {}

    // @Trace(metricName = "CallStateObserver.execute()")
    default void execute(Call call, CallState state) {
        switch (state) {
            case CONNECTION_ESTABLISHED:
                onConnectionEstablished(call);
                break;
            case STARTED:
                onStarted(call);
                break;
            case SECONDARY_STREAM_STARTED:
                onSecondStreamStarted(call);
                break;
            case ACTIVE_PROCESSING:
                onActiveProcessing(call);
                break;
            case SECONDARY_STREAM_ENDED:
                onSecondStreamEnded(call);
                break;
            case ENDED:
                onEnded(call);
                break;
            case ENDED_FOR_TRANSFER:
                onEndedForTransfer(call);
                break;
        }
    }
}
