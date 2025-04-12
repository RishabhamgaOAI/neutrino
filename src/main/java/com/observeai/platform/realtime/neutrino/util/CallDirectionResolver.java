package com.observeai.platform.realtime.neutrino.util;

import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import com.observeai.platform.realtime.neutrino.data.dto.NiceEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.TalkdeskCallDto;
import com.observeai.platform.realtime.neutrino.data.dto.TwilioEventTaskAttributesDto;
import com.observeai.platform.realtime.neutrino.data.dto.five9.CallEvent;
import com.observeai.platform.realtime.neutrino.data.dto.five9.CallEvent.CallDetails;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.observeai.platform.realtime.neutrino.util.five9.Five9Util.FIVE9_INBOUND_CALL_TYPES;
import static com.observeai.platform.realtime.neutrino.util.five9.Five9Util.FIVE9_OUTBOUND_CALL_TYPES;

@Slf4j
public class CallDirectionResolver {
    private final List<String> inboundValues;
    private final List<String> outboundValues;

    public CallDirectionResolver(List<String> inboundValues, List<String> outboundValues) {
        this.inboundValues = inboundValues;
        this.outboundValues = outboundValues;
    }

    public CallDirectionResolver(String inboundValue, String outboundValue) {
        this.inboundValues = Collections.singletonList(inboundValue);
        this.outboundValues = Collections.singletonList(outboundValue);
    }

    protected Optional<CallDirection> getCallDirection(String value) {
        if (inboundValues.stream().anyMatch(value::equalsIgnoreCase))
            return Optional.of(CallDirection.INBOUND);
        if (outboundValues.stream().anyMatch(value::equalsIgnoreCase))
            return Optional.of(CallDirection.OUTBOUND);
        return Optional.empty();
    }

    public static class Five9CallDirectionResolver extends CallDirectionResolver {

        public Five9CallDirectionResolver() {
            super(FIVE9_INBOUND_CALL_TYPES, FIVE9_OUTBOUND_CALL_TYPES);
        }

        public CallDirection getCallDirection(CallEvent callEvent) {
            CallDetails callDetails = callEvent.getCallDetails();
            String typeName = callDetails.getTypeName();
            Optional<CallDirection> optionalCallDirection = getCallDirection(typeName);
            CallDirection callDirection = optionalCallDirection.orElseGet(() -> {
                log.warn("Received unknown type name for five9 call. DomainId: {}, CallId: {}, TypeName: {}",
                    callDetails.getDomainId(), callDetails.getCallId(), callDetails.getTypeName());
                return CallDirection.UNKNOWN;
            });

            log.info("Identified direction of five9 call with domainId: {}, callId: {}, typeName: {} as {}",
                callDetails.getDomainId(), callDetails.getCallId(), callDetails.getTypeName(), callDirection);
            return callDirection;
        }
    }

    public static class TwilioCallDirectionResolver extends CallDirectionResolver {

        public TwilioCallDirectionResolver() {
            super(Constants.INBOUND, Constants.OUTBOUND);
        }

        public CallDirection getCallDirection(TwilioEventTaskAttributesDto taskAttributesDto) {
            Optional<CallDirection> optionalCallDirection = getCallDirection(taskAttributesDto.getDirection());
            CallDirection callDirection = optionalCallDirection.orElseGet(() -> {
                log.warn("Received unknown type name for twilio call. Callsid: {}, direction: {}",
                    taskAttributesDto.getCallSid(), taskAttributesDto.getDirection());
                return CallDirection.UNKNOWN;
            });

            log.info("Identified direction of twilio call with callSid: {} as {}", taskAttributesDto.getCallSid(), callDirection);
            return callDirection;
        }
    }

    public static class TalkdeskCallDirectionResolver extends CallDirectionResolver {

        public TalkdeskCallDirectionResolver() {
            super(Constants.INBOUND, Constants.OUTBOUND);
        }

        public CallDirection getCallDirection(TalkdeskCallDto callDto) {
            Optional<CallDirection> optionalCallDirection = getCallDirection(callDto.getCallType());
            CallDirection callDirection = optionalCallDirection.orElseGet(() -> {
                log.warn("Received unknown type name for talkdesk call. Callid: {}, direction: {}",
                        callDto.getInteractionId(), callDto.getCallType());
                return CallDirection.UNKNOWN;
            });

            log.info("Identified direction of talkdesk call with callid: {} as {}", callDto.getInteractionId(), callDirection);
            return callDirection;
        }
    }

    public static class NiceCallDirectionResolver extends CallDirectionResolver {

        public NiceCallDirectionResolver() {
            super(Constants.INBOUND, Constants.OUTBOUND);
        }

        public CallDirection getCallDirection(NiceEventDto callDto) {
            Optional<CallDirection> optionalCallDirection = getCallDirection(callDto.getCallDirection());
            CallDirection callDirection = optionalCallDirection.orElseGet(() -> {
                log.warn("Received unknown type name for nice call. Callid: {}, direction: {}",
                        callDto.getContactId(), callDto.getCallDirection());
                return CallDirection.UNKNOWN;
            });

            log.info("Identified direction of nice call with callid: {} as {}", callDto.getContactId(), callDirection);
            return callDirection;
        }
    }
}
