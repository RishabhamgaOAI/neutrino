package com.observeai.platform.realtime.neutrino.service.impl.five9;

import com.observeai.platform.realtime.neutrino.client.five9.SubscriptionClient;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionNotification;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionResponse;
import com.observeai.platform.realtime.neutrino.service.five9.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionClient subscriptionClient;

    private SubscriptionResponse getSubscriptionResponse(SubscriptionNotification notification, String status) {
        SubscriptionResponse response = subscriptionClient.getSubscriptionById(
                notification.getDomainId(), notification.getSubscriptionId());
        response.setStatus(status);
        return response;
    }

    @Override
    public SubscriptionResponse onCreate(SubscriptionNotification notification) {
        return getSubscriptionResponse(notification, "active");
    }

    @Override
    public SubscriptionResponse onUpdate(SubscriptionNotification notification) {
        return null;
    }

    @Override
    public SubscriptionResponse onDelete(SubscriptionNotification notification) {
        return null;
    }
}
