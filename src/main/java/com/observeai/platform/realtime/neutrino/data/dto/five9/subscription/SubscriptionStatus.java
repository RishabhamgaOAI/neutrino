package com.observeai.platform.realtime.neutrino.data.dto.five9.subscription;

public enum SubscriptionStatus {
    PENDING("pending"),
    ACTIVE("active"),
    DELETED("deleted"),
    ERROR("error");

    private String value;

    SubscriptionStatus(String value) {
        this.value = value;
    }
}
