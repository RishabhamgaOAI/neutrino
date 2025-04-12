package com.observeai.platform.realtime.neutrino.data.dto.five9.subscription;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SubscriptionResponse {
    private String subscriptionId;
    private String directiveId;
    private String name;
    private String clientId;
    private List<Filter> filters;
    private String status;
    private String uri;
    private String createdBy;
    private String createdOn;
    private String lastModifiedBy;
    private String lastModifiedOn;
}
