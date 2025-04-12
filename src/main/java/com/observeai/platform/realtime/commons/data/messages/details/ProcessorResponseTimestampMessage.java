package com.observeai.platform.realtime.commons.data.messages.details;

import com.observeai.platform.realtime.neutrino.enums.Speaker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ProcessorResponseTimestampMessage extends TimestampMessage {
    private long startTimestamp;
    private Speaker speaker;
    private double transcriptStart;
    private double transcriptDuration;
}
