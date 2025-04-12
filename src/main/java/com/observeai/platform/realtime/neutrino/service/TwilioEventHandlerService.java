package com.observeai.platform.realtime.neutrino.service;

import com.observeai.platform.realtime.neutrino.data.dto.TwilioEventDto;

public interface TwilioEventHandlerService {
    void handleCallStartEvent(TwilioEventDto twilioEventDto);

    void handleCallEndEvent(TwilioEventDto twilioEventDto);
}
