package com.observeai.platform.realtime.neutrino.data.dto.five9.subscription;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Getter
@Setter
@ToString
public class SubscriptionNotification {
    private String description;
    private String subscriptionId;
    private String domainId;
    private String clientId;
    private String notificationType;
    private String createdOn;
    private String modifiedOn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionNotification that = (SubscriptionNotification) o;
        return Objects.equals(description, that.description) && Objects.equals(subscriptionId, that.subscriptionId) && Objects.equals(domainId, that.domainId) && Objects.equals(clientId, that.clientId) && Objects.equals(notificationType, that.notificationType) && Objects.equals(createdOn, that.createdOn) && Objects.equals(modifiedOn, that.modifiedOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, subscriptionId, domainId, clientId, notificationType, createdOn, modifiedOn);
    }
}
