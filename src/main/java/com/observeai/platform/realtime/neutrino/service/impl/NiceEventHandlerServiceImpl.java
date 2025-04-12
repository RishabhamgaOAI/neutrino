package com.observeai.platform.realtime.neutrino.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.integration.commons.objectmapper.ObjectMapperFactory;
import com.observeai.platform.realtime.neutrino.data.common.AdditionalCallConfiguration;
import com.observeai.platform.realtime.neutrino.data.dto.CallSummaryProperties;
import com.observeai.platform.realtime.neutrino.data.dto.AccountAndUserInfoResponseDto;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.NiceEventDto;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.repository.AdditionalCallConfigurationRepository;
import com.observeai.platform.realtime.neutrino.service.NiceEventHandlerService;
import com.observeai.platform.realtime.neutrino.util.CallBackMetaEventMapper;
import com.observeai.platform.realtime.neutrino.util.CallDirectionResolver;
import com.observeai.platform.realtime.neutrino.util.DravityRequestUtil;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static com.observeai.platform.realtime.neutrino.util.Constants.CALL_END;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class NiceEventHandlerServiceImpl implements NiceEventHandlerService {

    private final KafkaProducer producer;
    private final KafkaProperties kafkaProperties;
    private final DravityRequestUtil dravityRequestUtil;
    private final AdditionalCallConfigurationRepository additionalCallConfigurationRepository;

    private static final CallDirectionResolver.NiceCallDirectionResolver directionResolver = new CallDirectionResolver.NiceCallDirectionResolver();
    private static final ObjectMapper pascalCaseObjectMapper = ObjectMapperFactory.getPascalCaseObjectMapper();
    private static final String NICE = "NICE";

    @Override
    public void handleCallStartEvent(NiceEventDto niceEventDto, AccountAndUserInfoResponseDto accountAndUserInfo) {
    }

    @Override
    public void handleCallHoldEvent(NiceEventDto niceEventDto, AccountAndUserInfoResponseDto accountAndUserInfo) {
        boolean isUpdated = updateNiceEventDtoAndAcknowledge(niceEventDto, accountAndUserInfo);
        if(!isUpdated) return;
        publishNiceEvent(niceEventDto);
    }

    @Override
    public void handleCallResumeEvent(NiceEventDto niceEventDto, AccountAndUserInfoResponseDto accountAndUserInfo) {
        boolean isUpdated = updateNiceEventDtoAndAcknowledge(niceEventDto, accountAndUserInfo);
        if(!isUpdated) return;
        publishNiceEvent(niceEventDto);
    }

    @Override
    public void handleCallEndEvent(NiceEventDto niceEventDto, AccountAndUserInfoResponseDto accountAndUserInfo) {
        boolean isUpdated = updateNiceEventDtoAndAcknowledge(niceEventDto, accountAndUserInfo);
        if(!isUpdated) return;
        publishNiceEvent(niceEventDto);
    }

    private boolean updateNiceEventDtoAndAcknowledge(NiceEventDto niceEventDto, AccountAndUserInfoResponseDto accountAndUserInfo) {
        log.info("updating observe details in NICE call back event of type: {} for accountId={}, agentId={}, callId={}", niceEventDto.getEvent(), niceEventDto.getAccountId(), niceEventDto.getAgentId(), niceEventDto.getContactId());
        if (accountAndUserInfo == null) {
            log.info("not able to update observe details in NICE call back event of type: {} for accountId={}, agentId={}, callId={}", niceEventDto.getEvent(), niceEventDto.getAccountId(), niceEventDto.getAgentId(), niceEventDto.getContactId());
            return false;
        }

        niceEventDto.setObserveAccountId(accountAndUserInfo.getAccountInfo().getObserveAccountId());
        niceEventDto.setObserveUserId(accountAndUserInfo.getUserMapping().getObserveUserId());
        niceEventDto.setDirection(directionResolver.getCallDirection(niceEventDto));
        log.info("updated observe details in NICE call back event of type: {} for accountId={}, agentId={}, callId={}",niceEventDto.getEvent(), niceEventDto.getAccountId(), niceEventDto.getAgentId(), niceEventDto.getContactId());

        if (CALL_END.equals(niceEventDto.getEvent())) {
            CallSummaryProperties.PushSummaryType pushSummaryType = Optional.ofNullable(accountAndUserInfo.getAccountInfo())
                    .map(AccountInfoWithVendorDetailsDto::getCallSummaryProperties)
                    .map(CallSummaryProperties::getPushSummaryType).orElse(CallSummaryProperties.PushSummaryType.NONE);

            if (pushSummaryType != CallSummaryProperties.PushSummaryType.NONE) {
                log.info("summary push enabled for accountId={}. reading additional call configuration for agentId={}, callId={}", niceEventDto.getAccountId(), niceEventDto.getAgentId(), niceEventDto.getContactId());
                try {
                    Map<String, String> additionalParams = pascalCaseObjectMapper.readValue(niceEventDto.getAdditionalParams(), new TypeReference<>() {});
                    AdditionalCallConfiguration additionalCallConfiguration = new AdditionalCallConfiguration(niceEventDto.getObserveUserId(), niceEventDto.getContactId(), niceEventDto.getObserveAccountId(), additionalParams);
                    additionalCallConfigurationRepository.save(additionalCallConfiguration);
                    log.info("additional call configuration saved for accountId={}, agentId={}, callId={}", niceEventDto.getAccountId(), niceEventDto.getAgentId(), niceEventDto.getContactId());
                } catch (Throwable th) {
                    log.error("error while pushing additional call configuration for accountId={}, agentId={}, callId={}, event={}, due to error={}",
                            niceEventDto.getAccountId(), niceEventDto.getAgentId(), niceEventDto.getContactId(), niceEventDto.toString(), th.toString(), th);
                }
            } else {
                log.info("summary push not enabled for accountId={}. not reading additional call configuration for agentId={}, callId={}", niceEventDto.getAccountId(), niceEventDto.getAgentId(), niceEventDto.getContactId());
            }
        }

        return true;
    }

    private void publishNiceEvent(NiceEventDto niceEventDto) {
        log.info("publishing NICE call back event of type: {} for accountId={}, agentId={}, callId={}", niceEventDto.getEvent(), niceEventDto.getAccountId(), niceEventDto.getAgentId(), niceEventDto.getContactId());
        CallBackMetaEventDto callBackMetaEventDto = CallBackMetaEventMapper.from(niceEventDto);
        producer.produceMessage(kafkaProperties.getTopics().getCallBackMetaEventsTopic(), niceEventDto.getContactId(), callBackMetaEventDto);
//        producer.produceProtoMessage(kafkaProperties.getTopics().getCallBackMetaEventsProtoTopic(), niceEventDto.getContactId(), CallBackMetaEventMapper.toProto(callBackMetaEventDto));
        log.info("published NICE call back event of type: {} for accountId={}, agentId={}, callId={}", niceEventDto.getEvent(), niceEventDto.getAccountId(), niceEventDto.getAgentId(), niceEventDto.getContactId());
    }

    public AccountAndUserInfoResponseDto getAccountAndUserInfo(String vendorCallId, String vendorAccountId, String vendorUserId){
        final HttpResponse<AccountAndUserInfoResponseDto> httpResponse;
        try {
            httpResponse = dravityRequestUtil.getAccountAndUserInfo(NICE, vendorAccountId, vendorUserId);
        } catch (URISyntaxException e) {
            log.error("dravity api exception. error={}", e.getMessage());
            return null;
        }
        if (httpResponse.hasError()) {
            log.error("error in getting response from dravity for callId={}", vendorCallId, httpResponse.getError().getCause());
            return null;
        }
        return httpResponse.getResponse();
    }
}
