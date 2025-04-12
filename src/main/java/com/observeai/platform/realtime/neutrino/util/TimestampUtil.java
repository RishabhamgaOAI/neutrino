package com.observeai.platform.realtime.neutrino.util;

import com.observeai.platform.realtime.commons.data.messages.details.DeepgramMessage;
import com.observeai.platform.realtime.neutrino.data.dto.CallProcessingEventTimestamp;
import com.observeai.platform.realtime.neutrino.data.dto.MomentDto;
import com.observeai.platform.realtime.neutrino.enums.CallProcessingEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TimestampUtil {

    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

public static CallProcessingEventTimestamp.CallProcessorResponseTimestamp buildProcessingResponseTimestamp(
            long startTimestamp, long eventTimestamp, DeepgramMessage deepgramResponse, CallProcessingEvent processingEvent,
            List<MomentDto> moments) {
        return CallProcessingEventTimestamp.CallProcessorResponseTimestamp.builder()
                .startTimestamp(startTimestamp)
                .eventTimestamp(eventTimestamp).speaker(deepgramResponse.getSpeaker())
                .audioOffset(deepgramResponse.getStart()).audioDuration(deepgramResponse.getDuration())
                .processingEvent(processingEvent).moments(moments).build();
    }


public static CallProcessingEventTimestamp.CallProcessorResponseTimestamp buildProcessingResponseTimestamp(
            long startTimestamp, long eventTimestamp, DeepgramMessage deepgramResponse, CallProcessingEvent processingEvent) {
        return buildProcessingResponseTimestamp(startTimestamp, eventTimestamp, deepgramResponse, processingEvent, null);
    }

}
