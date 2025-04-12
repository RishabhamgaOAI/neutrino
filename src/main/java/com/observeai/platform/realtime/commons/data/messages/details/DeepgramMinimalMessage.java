package com.observeai.platform.realtime.commons.data.messages.details;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeepgramMinimalMessage {
    private double start;
    private double end;
    private String transcript;
    private boolean finall;
}
