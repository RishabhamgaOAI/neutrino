package com.observeai.platform.realtime.neutrino.util;

import com.observeai.platform.realtime.neutrino.client.DravityProperties;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallAttachedMetadata;
import com.observeai.platform.realtime.neutrino.data.dto.AccountAndUserInfoResponseDto;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoConcise;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto.VendorAccountDetailsDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.exception.handler.ExceptionHandlerFactory;
import com.observeai.platform.realtime.neutrino.exception.handler.ExtAppExceptionHandler;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import com.observeai.platform.realtime.neutrino.util.http.RestTemplateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URISyntaxException;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DravityRequestUtil {
  private final DravityProperties dravityProperties;
  private final RestTemplateWrapper restTemplateWrapper;
  private final ExtAppExceptionHandler exceptionHandler = ExceptionHandlerFactory.getDravityExceptionHandler();

  public HttpResponse<AccountAndUserInfoResponseDto> getAccountAndUserInfo(String vendor, String vendorAccountId, String agentId) throws URISyntaxException {
    String url = UriComponentsBuilder.fromHttpUrl(dravityProperties.accountAndUserInfoByVendorUserIdUrl()).build(vendor, vendorAccountId, agentId).toString();
    HttpEntity<Void> httpEntity = new HttpEntity<>(null, null);
    return restTemplateWrapper.exchangeRequest(url, HttpMethod.GET, httpEntity, AccountAndUserInfoResponseDto.class, exceptionHandler);
  }

  public HttpResponse<AccountInfoWithVendorDetailsDto> getAccountInfo(String vendor, String vendorAccountId) throws URISyntaxException {
    String url = UriComponentsBuilder.fromHttpUrl(dravityProperties.accountInfoByVendorAccountIdUrl()).build(vendor, vendorAccountId).toString();
    HttpEntity<Void> httpEntity = new HttpEntity<>(null, null);
    return restTemplateWrapper.exchangeRequest(url, HttpMethod.GET, httpEntity, AccountInfoWithVendorDetailsDto.class, exceptionHandler);
  }

  public HttpResponse<AccountInfoConcise> getAccountInfoByObserveAccountId(String observeAccountId) {
    String url = UriComponentsBuilder.fromHttpUrl(dravityProperties.accountInfoByObserveAccountIdUrl()).build(observeAccountId).toString();
    HttpEntity<Void> httpEntity = new HttpEntity<>(null, null);
    return restTemplateWrapper.exchangeRequest(url, HttpMethod.GET, httpEntity, AccountInfoConcise.class, exceptionHandler);
  }

  public void updateVendorAccountDetails(String vendor, String vendorAccountId, String patch) {
    String url = UriComponentsBuilder.fromHttpUrl(dravityProperties.accountInfoByVendorAccountIdUrl()).build(vendor, vendorAccountId).toString();
      updateVendorAccountDetails(url, patch);
  }

  public void updateVendorAccountDetails(String url, String patch) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/json-patch+json"));
    HttpEntity<String> httpEntity = new HttpEntity<>(patch, headers);
    restTemplateWrapper.exchangeRequest(url, HttpMethod.PATCH, httpEntity, VendorAccountDetailsDto.class, exceptionHandler);
  }

  public HttpResponse<CallAttachedMetadata> getConfigsNeededForCall(Call call) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(dravityProperties.configsForCallByObserveIdsUrl());
    String previewExperienceId = Optional.of(call.getStartMessage()).map(CallStartMessage::getExperienceId).orElse(null);
    if (Strings.isNotBlank(previewExperienceId)) {
      builder = builder.queryParam("experienceId", previewExperienceId);
    }
    String url = builder.build(call.getStartMessage().getAccountId(), call.getStartMessage().getAgentId()).toString();
    log.info("observeCallId={}, fetching configs for call using url={}", call.getObserveCallId(), url);
    return restTemplateWrapper.exchangeRequest(url, HttpMethod.GET, null, CallAttachedMetadata.class, exceptionHandler);
  }
}
