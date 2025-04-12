package com.observeai.platform.realtime.neutrino.service.five9;

import com.observeai.platform.realtime.neutrino.data.dto.five9.directive.DirectiveResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface DirectiveService {
    Optional<String> getDirectiveId(String domainId);
    DirectiveResponse createDirective(String domainId);
    void attachDirectiveToSubscription(String domainId, String directiveId, String subscriptionId);
}
