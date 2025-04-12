package com.observeai.platform.realtime.neutrino.data.dto;

import com.observeai.platform.realtime.neutrino.config.CallSourceConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.Base64;

@Getter
@Setter
public class CallAudio {
    private String encodedAudio;
    private int sampleRate;
    private int channels;
    private String encoding;

    public CallAudio(String encodedAudio, CallSourceConfig callSourceConfig) {
        this.encodedAudio = encodedAudio;
        this.sampleRate = callSourceConfig.getSampleRate();
        this.channels = callSourceConfig.getChannels();
        this.encoding = callSourceConfig.getEncoding();
    }

    public static CallAudio fromStereoData(byte[] stereoData, CallSourceConfig callSourceConfig) {
        String encodedAudio = Base64.getEncoder().encodeToString(stereoData);
        return new CallAudio(encodedAudio, callSourceConfig);
    }
}
