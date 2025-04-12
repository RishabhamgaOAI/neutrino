package com.observeai.platform.realtime.neutrino.util;

import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class CallEventJoinerUtil {

    public static boolean preValidate(Optional<CallEventDto> callEventDto, Optional<CallBackMetaEventDto> callBackMetaEventDto) {
        if (!callEventDto.isPresent() && !callBackMetaEventDto.isPresent()) {
            log.error("Both callEventDto and callBackMetaEventDto are missing");
            return false;
        } else if (!callEventDto.isPresent()) {
            log.warn("VendorCallId: {}, Validation check for join failed. CallEventDto is null",
                    callBackMetaEventDto.get().getVendorCallId());
            return false;
        } else if (!callBackMetaEventDto.isPresent()) {
            log.warn("ObserveCallId: {}, VendorCallId: {}, Validation check for join failed. CallBackMetaEventDto is null",
                    callEventDto.get().getObserveCallId(), callEventDto.get().getVendorCallId());
            return false;
        } else {
            log.info("ObserveCallId: {}, VendorCallId: {}, Validation check for join passed",
                    callEventDto.get().getObserveCallId(), callEventDto.get().getVendorCallId());
            return true;
        }
    }
}
