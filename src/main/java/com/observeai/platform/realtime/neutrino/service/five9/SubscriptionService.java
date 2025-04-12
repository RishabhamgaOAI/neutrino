package com.observeai.platform.realtime.neutrino.service.five9;

import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionNotification;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionResponse;
import org.springframework.stereotype.Service;

@Service
public interface SubscriptionService {
    SubscriptionResponse onCreate(SubscriptionNotification notification);
    SubscriptionResponse onUpdate(SubscriptionNotification notification);
    SubscriptionResponse onDelete(SubscriptionNotification notification);
}
