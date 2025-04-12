package com.observeai.platform.realtime.neutrino.data.dto.five9.subscription;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubscriptionRequest {
    private String name;
    private String clientId;
    private List<Filter> filters;
}
