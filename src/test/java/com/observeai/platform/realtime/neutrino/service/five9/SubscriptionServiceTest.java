package com.observeai.platform.realtime.neutrino.service.five9;

import com.observeai.platform.realtime.neutrino.NeutrinoBaseTest;
import com.observeai.platform.realtime.neutrino.client.five9.SubscriptionClient;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionResponse;
import com.observeai.platform.realtime.neutrino.service.impl.five9.SubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.observeai.platform.realtime.neutrino.utils.Five9TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
class SubscriptionServiceTest extends NeutrinoBaseTest {

    @Mock
    private SubscriptionClient subscriptionClient;
    private SubscriptionService subscriptionService;

    @BeforeEach
    void init() {
        subscriptionService = new SubscriptionServiceImpl(subscriptionClient);
    }

    @Test
    void createSubscription_ValidResponse_ShouldReturnSubscriptionResponse() {
        when(subscriptionClient.getSubscriptionById(SAMPLE_DOMAIN_ID, SAMPLE_SUBSCRIPTION_ID)).thenReturn(sampleSubscriptionResponse());

        SubscriptionResponse response = subscriptionService.onCreate(sampleCreateSubscriptionNotification());
        assertThat(response.getStatus()).isEqualTo("active");
    }
}
