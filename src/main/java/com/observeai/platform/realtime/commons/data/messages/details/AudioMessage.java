package com.observeai.platform.realtime.commons.data.messages.details;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.util.AudioTrack;
import lombok.*;

import java.util.Base64;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(SnakeCaseStrategy.class)
public class AudioMessage {
    private String audio;
    private boolean recordAudio;
    private int byteSize;
    private int bufferSize;
    private int bytesPerSample;
    private int sampleRate;
    private int channels;
    private int audioOutputChannels;
    private String encoding;
    private boolean isSplitStream;
    private AudioTrack track;

    public AudioMessage(byte[] audioData, AudioTrack track, Call call) {
        this.recordAudio = call.getStartMessage().isRecordAudio();
        this.audio = Base64.getEncoder().encodeToString(audioData);
        this.byteSize = audioData.length;
        this.bufferSize = call.getCallSourceConfig().getBufferSize();
        this.bytesPerSample = call.getCallSourceConfig().getBytesPerSample();
        this.sampleRate = call.getCallSourceConfig().getSampleRate();
        this.channels = call.getCallSourceConfig().getChannels();
        this.audioOutputChannels = call.getCallSourceConfig().getAudioOutputChannels();
        this.encoding = call.getCallSourceConfig().getEncoding();
        this.isSplitStream = call.getCallSourceConfig().isSplitStream();
        this.track = track;
    }

}
