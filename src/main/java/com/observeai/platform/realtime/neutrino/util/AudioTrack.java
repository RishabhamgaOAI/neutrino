package com.observeai.platform.realtime.neutrino.util;

public enum AudioTrack {
    STEREO("stereo"),
    INBOUND("inbound"),
    OUTBOUND("outbound");

    private String value;

    AudioTrack(String value) {
        this.value = value;
    }

    public static AudioTrack fromString(String value) {
        for (AudioTrack audioTrack: AudioTrack.values()) {
            if (audioTrack.value.equalsIgnoreCase(value)) {
                return audioTrack;
            }
        }
        throw new IllegalArgumentException("invalid argument passed for AudioTrack");
    }
}
