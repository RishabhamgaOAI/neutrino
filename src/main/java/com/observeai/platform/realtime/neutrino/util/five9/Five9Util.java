package com.observeai.platform.realtime.neutrino.util.five9;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.client.PartnerAuthClient;
import com.observeai.platform.realtime.neutrino.data.dto.AccountAndUserInfoResponseDto;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto;
import com.observeai.platform.realtime.neutrino.data.dto.five9.Five9CustomerConfig;
import com.observeai.platform.realtime.neutrino.util.DravityRequestUtil;
import com.observeai.platform.realtime.neutrino.util.JsonPatchBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class Five9Util {
    public static final String FIVE9_VENDOR_NAME = "FIVE9";
    public static final List<String> FIVE9_OUTBOUND_CALL_TYPES = Arrays.asList("Manual", "Outbound", "Autodial");
    public static final List<String> FIVE9_INBOUND_CALL_TYPES = Collections.singletonList("Inbound");
    private final PartnerAuthClient pauthClient;
    private final DravityRequestUtil dravityRequestUtil;

    public HttpHeaders getHttpHeaderWithToken(String domainId) {
        HttpHeaders headers;
        try {
            headers = new HttpHeaders();
            AccountInfoWithVendorDetailsDto accountInfo = getAccountInfoByFive9DomainId(domainId);
            String cluster = accountInfo.getDeploymentCluster();
            String partnerUid = Optional.ofNullable(accountInfo.getVendorAccountDetails().getPartnerUid()).orElse(accountInfo.getName());
            headers.setBearerAuth(pauthClient.getToken(cluster, partnerUid).getAccessToken());
        } catch (Exception e) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return headers;
    }

    public void persistDirectiveId(String directiveId, String domainId) {
        String patch = new JsonPatchBuilder().path("/config").add("/directiveId", directiveId).build();
        dravityRequestUtil.updateVendorAccountDetails(FIVE9_VENDOR_NAME, domainId, patch);
    }

    public Optional<Five9CustomerConfig> convertToFive9CustomerConfig(Object config) {
        return Optional.ofNullable(new ObjectMapper().convertValue(config, Five9CustomerConfig.class));
    }

    public AccountInfoWithVendorDetailsDto getAccountInfoByFive9DomainId(String domainId)  {
        try {
            return dravityRequestUtil.getAccountInfo(FIVE9_VENDOR_NAME, domainId).getResponse();
        } catch (URISyntaxException e) {
            log.error("Unable to fetch account details by five9 domainId: " + domainId, e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public AccountAndUserInfoResponseDto getAccountAndUserInfo(String domainId, String agentId) {
        try {
            return dravityRequestUtil.getAccountAndUserInfo(FIVE9_VENDOR_NAME, domainId, agentId).getResponse();
        } catch (URISyntaxException e) {
            log.error("Unable to fetch account details by five9 domainId: " + domainId, e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
