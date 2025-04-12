package com.observeai.platform.realtime.neutrino.redis;

import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.DeepgramCallState;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.enums.Speaker;
import com.observeai.platform.realtime.neutrino.observer.CallStateObserver;
import com.observeai.platform.realtime.neutrino.util.DeepgramUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class DeepgramRedisStore extends RedisHashStoreByVendorCallId<Long> implements CallStateObserver {

    public DeepgramRedisStore(RedisTemplate<String, String> redisHashStoreTemplate) {
        super(redisHashStoreTemplate, Long.class);
    }

    public void pushStartTimestamp(Call call, long startTimestamp) {
        Speaker speaker = DeepgramUtil.getSpeakerByTrack(call.getTrack());
        log.info("vendorCallId={}, Pushing Deepgram first connection timestamp to redis store for {}", call.getStartMessage().getVendorCallId(), speaker.name());
        push(call.getStartMessage().getVendorCallId(), "start_timestamp_" + DeepgramUtil.getSpeakerByTrack(call.getTrack()).name(), startTimestamp);
    }

    public void pushSeqNumber(Call call, DeepgramCallState deepgramCallState) {
        Speaker speaker = DeepgramUtil.getSpeakerByTrack(call.getTrack());
        log.info("vendorCallId={}, Pushing Deepgram Seq Ids to redis store for {}", call.getStartMessage().getVendorCallId(), speaker.name());
        push(call.getStartMessage().getVendorCallId(), "seq_audio_" + speaker.name(), deepgramCallState.getAudioMessageSeqNum());
        push(call.getStartMessage().getVendorCallId(), "seq_transcript_" + speaker.name(), deepgramCallState.getTranscriptMessageSeqNum());
    }

    @Override
    public void onEnded(Call call) {
        Optional.ofNullable(call.getStartMessage()).map(CallStartMessage::getVendorCallId)
                .ifPresent(vendorCallId -> {
                    log.info("vendorCallId={}, removing deepgramId from redis store if exists", vendorCallId);
                    delete(vendorCallId);
                });
    }

    @Override
    protected String getKeySuffix() {
        return "deepgram";
    }
}
