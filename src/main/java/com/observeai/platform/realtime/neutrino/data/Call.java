package com.observeai.platform.realtime.neutrino.data;

import com.observeai.platform.realtime.neutrino.config.CallSourceConfig;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import com.observeai.platform.realtime.neutrino.data.common.CallSessionMetadata;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.data.dto.PreviewCallsTranscriptionConfigs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.DateTime;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Call {
    @Setter(lombok.AccessLevel.NONE)
    private String observeCallId;
    private String masterCallId;
    private String parentCallId;
    @Setter(lombok.AccessLevel.NONE)
    private CallStartMessage startMessage;
    private WebSocketSession callAudioSession;
    private CallSessionMetadata sessionMetadata;
    @Setter(lombok.AccessLevel.NONE)
    private CallState state;
    private String track;
    private String vendor;
    private boolean isSecondaryStream;
    private boolean isResume;
    private String secondaryStreamObserveCallId;
    private List<CallState> stateTransitions;
    private Long startTime;
    private CallSourceConfig callSourceConfig;
    private Long keepAliveTimeStamp;
    private boolean processMediaMessages;
    private Call parentCall;
    private Call childCall;
    private boolean callStreamerCall;
    private PreviewCallsTranscriptionConfigs previewCallsTranscriptionConfigs;
    private String accountName;
    private boolean monitoringEnabled;
    private boolean metadataBasedScriptsEnabled;

    public Call(String observeCallId, String masterCallId, String parentCallId,
                WebSocketSession audioSession, CallSessionMetadata metadata, String vendor) {
        this.observeCallId = observeCallId;
        this.masterCallId = masterCallId;
        this.parentCallId = parentCallId;
        this.callAudioSession = audioSession;
        this.sessionMetadata = metadata;
        this.state = CallState.INIT;
        this.startTime = DateTime.now().getMillis();
        this.stateTransitions = new ArrayList<>();
        this.startMessage = new CallStartMessage();
        this.vendor = vendor; // might be null, as we will get vendor from start message later in some cases
        this.callStreamerCall = false;
        this.processMediaMessages = false;
    }

    /**
     * we don't have default setter method for 'state', 'observeCallId', 'startMessage' fields.
     * observeCallId and startMessage can only be updated via CallRepository class, 'state' via CallStateManager class
     * added '_' to prefix so devs don't use it directly
     */
    public void _updateState(CallState state) {
        this.state = state;
        this.stateTransitions.add(state);
    }

    public void _setObserveCallId(String observeCallId) {
        this.observeCallId = observeCallId;
    }

    public void _setStartMessage(CallStartMessage message) {
        this.startMessage = message;
        if ((!this.startMessage.hasDirection()) && (this.sessionMetadata.isCallWatchCall())) {
            this.startMessage.setDirection(CallDirection.UNKNOWN);
        }
    }

    public void allowMediaMessageProcessing() {
        this.processMediaMessages = true;
    }

    public String getVendor() {
        if (vendor != null)
            return vendor;
        else
            return Optional.ofNullable(startMessage).map(CallStartMessage::getVendor).orElse("UNKNOWN");
    }

    public Call createChildCall(String childObserveCallId) {
        Call childCall = new Call(childObserveCallId, this.getMasterCallId(), this.getObserveCallId(),
                this.getCallAudioSession(), this.getSessionMetadata(), this.getVendor());
        childCall.track = this.getTrack();
        childCall.isResume = this.isResume();

        // TODO: what to do with secondary streams here?
        childCall.isSecondaryStream = this.isSecondaryStream();
        childCall.secondaryStreamObserveCallId = this.getSecondaryStreamObserveCallId();
        childCall.startMessage = CallStartMessage.buildChildCallStartMessage(this.getStartMessage(), childObserveCallId);
        childCall.callSourceConfig = callSourceConfig;
        this.childCall = childCall;
        childCall.parentCall = this;
        return childCall;
    }

    public String getCallInitiationObserveCallId(){
        return this.isSecondaryStream() ? this.getSecondaryStreamObserveCallId() : this.getObserveCallId();
    }

    public boolean isPrimaryStream() {
        return !isSecondaryStream;
    }

    public Long getElapsedTimeInSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
}
