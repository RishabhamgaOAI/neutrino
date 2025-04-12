package com.observeai.platform.realtime.neutrino.util;

import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Slf4j
public class AudioUtil {

    public static byte[] fromMonoToMultichannel(int bytesPerSample, byte[]... channels) {
        if (channels.length <= 1) {
            throw new IllegalArgumentException("At least 2 channels are required");
        }
        if (bytesPerSample < 1) {
            throw new IllegalArgumentException("Bytes per sample must be at lease 1");
        }

        int maxFrameCount = 0;
        for (byte[] channel : channels) {
            maxFrameCount = Math.max(maxFrameCount, channel.length);
        }
        byte[] multiChannelOutput = new byte[maxFrameCount * channels.length];

        for (int i = 0; i < channels.length; i++) {
            int pos = i * bytesPerSample;
            byte[] channel = channels[i];
            int j = 0;
            while (j < channel.length) {
                for (int k = 0; (k < bytesPerSample) && (j + k < channel.length); k++) {
                    multiChannelOutput[pos + k] = channel[j + k];
                }
                pos = pos + channels.length * bytesPerSample;
                j = j + bytesPerSample;
            }
        }

        return multiChannelOutput;
    }

    public static byte[] removeAudioHeaderData(byte[] input) {
        AudioInputStream audioInputStream;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(input));
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int c;
            while ((c = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                byteArrayOutputStream.write(buffer, 0, c);
            }
            audioInputStream.close();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception exception) {
            log.error("Could not remove audio header data");
            return input;
        }
    }
}
