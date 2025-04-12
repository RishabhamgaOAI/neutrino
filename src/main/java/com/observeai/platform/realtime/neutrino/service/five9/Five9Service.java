package com.observeai.platform.realtime.neutrino.service.five9;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionNotification;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionResponse;
import org.springframework.stereotype.Service;


@Service
public interface Five9Service {
    SubscriptionResponse handleSubscriptionNotification(SubscriptionNotification notification);
    void handleCallEvents(JsonNode jsonNode) throws JsonProcessingException;
}
