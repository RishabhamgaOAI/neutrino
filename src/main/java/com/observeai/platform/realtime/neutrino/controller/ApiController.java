package com.observeai.platform.realtime.neutrino.controller;

import com.observeai.platform.realtime.neutrino.data.dto.NiceEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.TalkdeskEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.TwilioEventDto;
import com.observeai.platform.realtime.neutrino.service.ApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(value = "/apis/v1")
@CrossOrigin
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiController {

    private final ApiService apiService;

    @PostMapping(value = "/twilioEventsCallback",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> twilioEventCallbackHandler(TwilioEventDto requestBody) {
        String taskChannelName = requestBody.getTaskChannelUniqueName();
        if (taskChannelName != null && taskChannelName.equals("voice")) {
            return apiService.handleTwilioEvent(requestBody);
        } else {
            log.info("Received event message for unsupported task channel = {} for accountId = {}", taskChannelName, requestBody.getAccountSid());
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping(value = "/talkdeskEventsCallback")
    public ResponseEntity<Object> talkdeskEventCallbackHandler(@RequestBody TalkdeskEventDto requestBody) {
        return apiService.handleTalkdeskEvent(requestBody);
    }

    @PostMapping(value = "/niceEventsCallback",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> niceEventCallbackHandler(NiceEventDto requestBody) {
        return apiService.handleNiceEvent(requestBody);
    }
}
