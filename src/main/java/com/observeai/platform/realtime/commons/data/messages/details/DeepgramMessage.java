package com.observeai.platform.realtime.commons.data.messages.details;

import com.observeai.platform.realtime.neutrino.enums.Speaker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeepgramMessage {
    private int[] channelIndex;
    private double duration;
    private double start;
    private boolean isFinal;
    private boolean speechFinal;
    private Channel channel;
    private String transactionKey;
    private Speaker speaker;

    public String getDefaultTranscript() {
        return channel.getAlternatives().get(0).getTranscript();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Channel {
        private List<Alternative> alternatives;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alternative {
        private String transcript;
        private double confidence;
        private List<Word> words;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Word {
        private String word;
        private double start;
        private double end;
        private double confidence;
    }
}
