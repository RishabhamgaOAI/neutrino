package com.observeai.platform.realtime.neutrino.data;

import com.observeai.platform.realtime.neutrino.enums.Speaker;
import lombok.Getter;
import lombok.ToString;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class DeepgramCallState {
	private final Call call;
	private final ByteArrayOutputStream inboundBuffer;
	private final ByteArrayOutputStream outboundBuffer;
	private double timeOffset;
	public long audioMessageSeqNum;
	public long transcriptMessageSeqNum;
	private final Map<Speaker, Long> recentDgMessageTimestampBySpeaker = new HashMap<>();
	private long nonProcessedAudioMessages;

	public DeepgramCallState(Call call, double timeOffset, long audioMessageSeqNum, long transcriptMessageSeqNum) {
		this.call = call;
		this.inboundBuffer = new ByteArrayOutputStream();
		this.outboundBuffer = new ByteArrayOutputStream();
		this.timeOffset = timeOffset;
		this.audioMessageSeqNum = audioMessageSeqNum;
		this.transcriptMessageSeqNum = transcriptMessageSeqNum;
		this.nonProcessedAudioMessages = 0;
	}

	public void registerCurrentDgMessage(Speaker speaker, long timestamp) {
		recentDgMessageTimestampBySpeaker.put(speaker, timestamp);
	}

	public Optional<Long> getRecentDgMessageTimestamp(Speaker speaker) {
		return Optional.ofNullable(recentDgMessageTimestampBySpeaker.get(speaker));
	}

	public void incrementNonProcessedAudioMessages() {
		nonProcessedAudioMessages++;
	}

	public void setTimeOffset(double timeOffset) {
		this.timeOffset = timeOffset;
	}
}
