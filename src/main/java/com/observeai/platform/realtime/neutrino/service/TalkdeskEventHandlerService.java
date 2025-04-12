package com.observeai.platform.realtime.neutrino.service;

import com.observeai.platform.realtime.neutrino.data.dto.TalkdeskEventDto;

public interface TalkdeskEventHandlerService {
    void handleCallStartEvent(TalkdeskEventDto talkdeskEventDto);

    void handleCallEndEvent(TalkdeskEventDto talkdeskEventDto);
}
