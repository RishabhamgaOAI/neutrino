package com.observeai.platform.realtime.neutrino.data.dto;

import com.observeai.platform.realtime.neutrino.config.CallSourceConfig;
import com.observeai.platform.realtime.neutrino.enums.CallProcessingEvent;
import com.observeai.platform.realtime.neutrino.enums.Speaker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public abstract class CallProcessingEventTimestamp {
    private final long sequenceNum;
    private final long eventTimestamp;
    private final CallProcessingEvent processingEvent;

    @Getter
    public static class CallAudioRequestTimestamp extends CallProcessingEventTimestamp {
        private final int audioByteSize;
        private final CallSourceConfig audioSourceConfig;

        @Builder
        public CallAudioRequestTimestamp(long sequenceNum, long timestamp, int audioByteSize, CallSourceConfig callSourceConfig) {
            super(sequenceNum, timestamp, CallProcessingEvent.CALL_AUDIO_REQUEST);
            this.audioByteSize = audioByteSize;
            this.audioSourceConfig = callSourceConfig;
        }
    }

    @Getter
    public static class CallProcessorResponseTimestamp extends CallProcessingEventTimestamp {
        private final long startTimestamp;
        private final Speaker speaker;
        private final double audioOffsetInCall;
        private final double audioDurationInCall;
        private final List<MomentDto> moments;

        @Builder
        public CallProcessorResponseTimestamp(long sequenceNum, long startTimestamp, long eventTimestamp, Speaker speaker,
                                              double audioOffset, double audioDuration, CallProcessingEvent processingEvent, List<MomentDto> moments) {
            super(sequenceNum, eventTimestamp, processingEvent);
            this.startTimestamp = startTimestamp;
            this.speaker = speaker;
            this.audioOffsetInCall = audioOffset;
            this.audioDurationInCall = audioDuration;
            this.moments = moments;
        }
    }
}
