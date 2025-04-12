package com.observeai.platform.realtime.neutrino.service;

import com.observeai.platform.realtime.neutrino.data.dto.AccountAndUserInfoResponseDto;
import com.observeai.platform.realtime.neutrino.data.dto.NiceEventDto;

public interface NiceEventHandlerService {
    void handleCallStartEvent(NiceEventDto niceEventDto, AccountAndUserInfoResponseDto accountAndUserInfo);
    void handleCallHoldEvent(NiceEventDto niceEventDto, AccountAndUserInfoResponseDto accountAndUserInfo);
    void handleCallResumeEvent(NiceEventDto niceEventDto, AccountAndUserInfoResponseDto accountAndUserInfo);
    void handleCallEndEvent(NiceEventDto niceEventDto, AccountAndUserInfoResponseDto accountAndUserInfo);
    AccountAndUserInfoResponseDto getAccountAndUserInfo(String vendorCallId, String vendorAccountId, String vendorUserId);
}
