package com.observeai.platform.realtime.neutrino.service.impl.five9;

import com.observeai.platform.realtime.neutrino.client.five9.DirectiveClient;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto;
import com.observeai.platform.realtime.neutrino.data.dto.five9.Five9CustomerConfig;
import com.observeai.platform.realtime.neutrino.data.dto.five9.directive.DirectiveRequest;
import com.observeai.platform.realtime.neutrino.data.dto.five9.directive.DirectiveResponse;
import com.observeai.platform.realtime.neutrino.data.dto.five9.directive.Grpc;
import com.observeai.platform.realtime.neutrino.service.five9.DirectiveService;
import com.observeai.platform.realtime.neutrino.util.five9.Five9Properties;
import com.observeai.platform.realtime.neutrino.util.five9.Five9Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DirectiveServiceImpl implements DirectiveService {

    private final DirectiveClient directiveClient;
    private final Five9Util five9Util;
    private final Five9Properties five9Properties;

    @Override
    public Optional<String> getDirectiveId(String domainId) {
        AccountInfoWithVendorDetailsDto accountInfoWithVendorDetailsDto = five9Util.getAccountInfoByFive9DomainId(domainId);
        return Optional.ofNullable(accountInfoWithVendorDetailsDto.getVendorAccountDetails())
                .map(AccountInfoWithVendorDetailsDto.VendorAccountDetailsDto::getConfig)
                .map(AccountInfoWithVendorDetailsDto.VendorAccountConfig::getDirectiveId);
    }

    @Override
    public DirectiveResponse createDirective(String domainId) {
        return directiveClient.createDirective(domainId, getDirectiveRequest());
    }

    @Override
    public void attachDirectiveToSubscription(String domainId, String directiveId, String subscriptionId) {
        directiveClient.attachDirectiveToSubscription(domainId, directiveId, subscriptionId);
    }

    private DirectiveRequest getDirectiveRequest() {
        return new DirectiveRequest(five9Properties.getTrustToken(),
                new Grpc("both", five9Properties.getGrpcTargetUrl()),
                five9Properties.getCallEventUrl(),
                five9Properties.getVoiceStreamEventUrl()
        );
    }
}
