package com.observeai.platform.realtime.neutrino.config;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class CallSourceConfig {
    private int bufferSize;
    private int bytesPerSample;
    private int sampleRate;
    private int channels;
    private int audioOutputChannels;
    private boolean removeHeaderData;
    private String encoding;
    private boolean splitStream;
}
