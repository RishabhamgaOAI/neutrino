package com.observeai.platform.realtime.neutrino.service;

import com.observeai.platform.realtime.neutrino.data.dto.NiceEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.TalkdeskEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.TwilioEventDto;
import org.springframework.http.ResponseEntity;

public interface ApiService {
    ResponseEntity<Object> handleTwilioEvent(TwilioEventDto twilioEventDto);
    ResponseEntity<Object> handleTalkdeskEvent(TalkdeskEventDto talkdeskEventDto);
    ResponseEntity<Object> handleNiceEvent(NiceEventDto niceEventDto);
}
