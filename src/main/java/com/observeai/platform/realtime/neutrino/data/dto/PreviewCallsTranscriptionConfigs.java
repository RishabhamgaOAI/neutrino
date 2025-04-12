package com.observeai.platform.realtime.neutrino.data.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewCallsTranscriptionConfigs implements Serializable {
    private Map<Integer, String> channelMap;
}
