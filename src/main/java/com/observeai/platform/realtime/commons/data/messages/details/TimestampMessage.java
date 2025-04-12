package com.observeai.platform.realtime.commons.data.messages.details;

import com.observeai.platform.realtime.commons.data.enums.TimestampEvent;
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
public class TimestampMessage {
    private long sequenceNum;
    private long eventTimestamp;
    private TimestampEvent eventName;
}
