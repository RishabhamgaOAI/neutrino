package com.observeai.platform.realtime.neutrino.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionNotification;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionResponse;
import com.observeai.platform.realtime.neutrino.service.five9.Five9Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(value = "/apis/v1/five9")
@CrossOrigin
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Five9Controller {
    private final Five9Service five9Service;

    @PostMapping(value = "/subscriptions",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<SubscriptionResponse> subscriptionNotificationCallback(@RequestBody SubscriptionNotification notification) {
        log.info("Received five9 subscription notification={}", notification);
        SubscriptionResponse response = five9Service.handleSubscriptionNotification(notification);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = "/callevents")
    @ResponseStatus(HttpStatus.OK)
    public void handleCallEvents(@RequestBody JsonNode jsonNode) {
        try{
            five9Service.handleCallEvents(jsonNode);
        }
        catch (Exception e) {
            log.error("Encountered an error: {}, while handling five9 call event: {}", e.getMessage(), jsonNode.toString(), e);
        }
    }

    @PostMapping(value = "/voicestream/events/subscriptions/{subscriptionId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> handleVoiceStreamEvents(@PathVariable String subscriptionId, @RequestBody JsonNode jsonNode) {
        log.info("Five9 subscriptionId={}, voicestream event={}", subscriptionId, jsonNode.toString());
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

}
