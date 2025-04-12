package com.observeai.platform.realtime.neutrino.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;
import org.springframework.web.socket.BinaryMessage;

import java.util.Base64;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RawAudioMessage {
    private byte[] audioData;
    private AudioTrack track;
    private final long timestamp = TimestampUtil.getCurrentTimeMillis();

    public static RawAudioMessage fromJsonMessage(JSONObject message) {
        JSONObject media = message.getJSONObject("media");
        String payload = media.getString("payload");
        byte[] audioData = Base64.getDecoder().decode(payload);
        AudioTrack track = AudioTrack.fromString(media.getString("track"));
        return new RawAudioMessage(audioData, track);
    }

    public static RawAudioMessage fromBinaryMessage(BinaryMessage message, AudioTrack track) {
        return new RawAudioMessage(message.getPayload().array(), track);
    }
}
