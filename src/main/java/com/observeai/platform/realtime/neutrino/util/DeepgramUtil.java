package com.observeai.platform.realtime.neutrino.util;

import com.observeai.platform.realtime.commons.data.messages.details.DeepgramMessage;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.DeepgramCallState;
import com.observeai.platform.realtime.neutrino.enums.Speaker;
import com.observeai.platform.realtime.neutrino.redis.DeepgramRedisStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DeepgramUtil {
    private final DeepgramRedisStore deepgramRedisStore;

    public static List<String> getWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(text.trim().split("\\s+"));
    }

    public void updateDgMessage(DeepgramMessage deepgramMessage, DeepgramCallState deepgramCallState) {
        Speaker speaker = getSpeaker(deepgramMessage, deepgramCallState);
        deepgramMessage.setSpeaker(speaker);
        addTimeOffset(deepgramMessage, deepgramCallState);
    }

    private void addTimeOffset(DeepgramMessage deepgramResponse, DeepgramCallState deepgramCallState) {
        double timeOffset = deepgramCallState.getTimeOffset();
        if (timeOffset == 0)
            return;

        deepgramResponse.setStart(deepgramResponse.getStart() + timeOffset);
        deepgramResponse.getChannel().getAlternatives().forEach(
                alternative -> alternative.getWords().forEach(
                        word -> {
                            word.setStart(word.getStart() + timeOffset);
                            word.setEnd(word.getEnd() + timeOffset);}));
    }

    private Speaker getSpeaker(DeepgramMessage deepgramResponse, DeepgramCallState deepgramCallState) {
        if(Objects.nonNull(deepgramResponse.getTransactionKey()))
            return null;

        if (deepgramCallState.getCall().getTrack() == null) {
            int channelIndex = deepgramResponse.getChannelIndex()[0];
            if (deepgramCallState.getCall().getStartMessage().isPreviewCall()) {
                if (deepgramCallState.getCall().getPreviewCallsTranscriptionConfigs() != null) {
                    Map<Integer, String> channelMap = deepgramCallState.getCall().getPreviewCallsTranscriptionConfigs().getChannelMap();
                    if (channelMap != null && channelMap.containsKey(channelIndex)) {
                        String speakerValue = channelMap.get(channelIndex);
                        return Speaker.valueOf(speakerValue);
                    }
                }
            }
            return channelIndex == 1 ? Speaker.AGENT : Speaker.CUSTOMER;
        }
        return getSpeakerByTrack(deepgramCallState.getCall().getTrack());
    }

    public static Speaker getSpeakerByTrack(String track) {
        if (track == null) return Speaker.BOTH;

        Speaker speaker = null;
        switch (track) {
            case "inbound" :
                speaker = Speaker.CUSTOMER;
                break;
            case "outbound" :
                speaker = Speaker.AGENT;
                break;
        }
        return speaker;
    }

    public Long getSeqNumber(Call call, String type) {
        if (!call.isSecondaryStream()) return 1L;

        Speaker speaker = getSpeakerByTrack(call.getTrack());
        Optional<Long> seqNum = deepgramRedisStore.optionalGet(call.getStartMessage().getVendorCallId(), type + "_" + speaker.name());
        if (seqNum.isPresent()) {
            return seqNum.get() + 1;
        }
        return 1000000000L;
    }

    public double computeTimeOffset(Call call) {
        long currTimestamp = System.currentTimeMillis();
        Speaker speaker = getSpeakerByTrack(call.getTrack());
        Optional<Long> startTimestampOptional = deepgramRedisStore.optionalGet(call.getStartMessage().getVendorCallId(), "start_timestamp_" + speaker.name());
        if (startTimestampOptional.isEmpty()) {
            deepgramRedisStore.pushStartTimestamp(call, currTimestamp);
            return 0;
        }
        double timeOffset = (currTimestamp - startTimestampOptional.get())/1000.0;
        if (timeOffset < 0) {
            log.error("detected negative dg time offset of {} for callInitiationObserveCallId={}, observeCallId={}", timeOffset, call.getCallInitiationObserveCallId(), call.getObserveCallId());
            timeOffset = 0;
        }

        log.info("detected dg time offset of {} for callInitiationObserveCallId={}, observeCallId={}", timeOffset, call.getCallInitiationObserveCallId(), call.getObserveCallId());
        return timeOffset;
    }
}
