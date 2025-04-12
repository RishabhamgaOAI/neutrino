package com.observeai.platform.realtime.neutrino.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.data.dto.*;

import java.util.Map;

import static com.observeai.platform.realtime.neutrino.util.TwilioConstants.*;

public class CallBackMetaEventMapper {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final String TWILIO_VENDOR_NAME = "Twilio";
    private static final String TALKDESK_VENDOR_NAME = "Talkdesk";
    private static final String NICE_VENDOR_NAME = "NICE";

    public static CallBackMetaEventDto from(TwilioEventDto twilioEventDto) {
        CallBackMetaEventDto callBackMetaEventDto = new CallBackMetaEventDto();
        callBackMetaEventDto.setObserveAccountId(twilioEventDto.getObserveAccountId());
        callBackMetaEventDto.setObserveUserId(twilioEventDto.getObserveUserId());
        callBackMetaEventDto.setDirection(twilioEventDto.getDirection());
        callBackMetaEventDto.setVendorName(TWILIO_VENDOR_NAME);
        callBackMetaEventDto.setVendorAccountId(twilioEventDto.getAccountSid());
        callBackMetaEventDto.setVendorAgentId(twilioEventDto.getWorkerSid());

        if (RESERVATION_CREATED.equals(twilioEventDto.getEventType())) {
            callBackMetaEventDto.setCallEventType(CallBackMetaEventType.START_EVENT);
        } else if (RESERVATION_WRAPUP.equals(twilioEventDto.getEventType()) || RESERVATION_COMPLETED.equals(twilioEventDto.getEventType()) ||
                RESERVATION_CANCELLED.equals(twilioEventDto.getEventType())) {
            callBackMetaEventDto.setCallEventType(CallBackMetaEventType.END_EVENT);
        }

        Map<String, String> eventMetadata = objectMapper.convertValue(twilioEventDto, new TypeReference<>() {});
        callBackMetaEventDto.setEventMetadata(eventMetadata);
        return callBackMetaEventDto;
    }

    public static CallBackMetaEventDto from(TalkdeskEventDto talkdeskEventDto) {
        CallBackMetaEventDto callBackMetaEventDto = new CallBackMetaEventDto();
        callBackMetaEventDto.setObserveAccountId(talkdeskEventDto.getObserveAccountId());
        callBackMetaEventDto.setObserveUserId(talkdeskEventDto.getObserveUserId());
        callBackMetaEventDto.setDirection(talkdeskEventDto.getDirection());
        callBackMetaEventDto.setVendorName(TALKDESK_VENDOR_NAME);
        callBackMetaEventDto.setVendorAccountId(talkdeskEventDto.getAccountId());
        callBackMetaEventDto.setVendorAgentId(talkdeskEventDto.getAgentId());
        if (talkdeskEventDto.getEventType().equals(Constants.CALL_ANSWERED)) {
            callBackMetaEventDto.setCallEventType(CallBackMetaEventType.START_EVENT);
        } else if (talkdeskEventDto.getEventType().equals(Constants.CALL_FINISHED)) {
            callBackMetaEventDto.setCallEventType(CallBackMetaEventType.END_EVENT);
        }

        Map<String, String> eventMetadata = objectMapper.convertValue(talkdeskEventDto, new TypeReference<>() {});
        callBackMetaEventDto.setEventMetadata(eventMetadata);
        return callBackMetaEventDto;
    }

    public static CallBackMetaEventDto from(NiceEventDto niceEventDto) {
        CallBackMetaEventDto callBackMetaEventDto = new CallBackMetaEventDto();
        callBackMetaEventDto.setObserveAccountId(niceEventDto.getObserveAccountId());
        callBackMetaEventDto.setObserveUserId(niceEventDto.getObserveUserId());
        callBackMetaEventDto.setDirection(niceEventDto.getDirection());
        callBackMetaEventDto.setVendorName(NICE_VENDOR_NAME);
        callBackMetaEventDto.setVendorAccountId(niceEventDto.getAccountId());
        callBackMetaEventDto.setVendorAgentId(niceEventDto.getAgentId());
        callBackMetaEventDto.setVendorCallId(niceEventDto.getContactId());
        if (niceEventDto.getEvent().equals(Constants.CALL_START)) {
            callBackMetaEventDto.setCallEventType(CallBackMetaEventType.START_EVENT);
        } else if (niceEventDto.getEvent().equals(Constants.CALL_HOLD)) {
            callBackMetaEventDto.setCallEventType(CallBackMetaEventType.HOLD_EVENT);
        } else if (niceEventDto.getEvent().equals(Constants.CALL_RESUME)) {
            callBackMetaEventDto.setCallEventType(CallBackMetaEventType.RESUME_EVENT);
        } else if (niceEventDto.getEvent().equals(Constants.CALL_END)) {
            callBackMetaEventDto.setCallEventType(CallBackMetaEventType.END_EVENT);
        }

        Map<String, String> eventMetadata = objectMapper.convertValue(niceEventDto, new TypeReference<>() {});
        callBackMetaEventDto.setEventMetadata(eventMetadata);
        return callBackMetaEventDto;
    }

    public static com.observeai.platform.realtime.proto.CallBackMetaEventProto.CallEventTypeProto toCallEventTypeProto(CallBackMetaEventType callEventType) {
        return com.observeai.platform.realtime.proto.CallBackMetaEventProto.CallEventTypeProto.valueOf(callEventType.name());
    }

//    public static com.observeai.platform.realtime.proto.CallBackMetaEventProto.CallBackMetaEvent toProto(CallBackMetaEventDto dto) {
//        com.observeai.platform.realtime.proto.CallBackMetaEventProto.CallBackMetaEvent.Builder builder = com.observeai.platform.realtime.proto.CallBackMetaEventProto.CallBackMetaEvent.newBuilder();
//
//        if (dto.getCallEventType() != null) {
//            builder.setCallEventType(toCallEventTypeProto(dto.getCallEventType()));
//        }
//        if (dto.getDirection() != null) {
//            builder.setDirection(dto.getDirection().getValue());
//        }
//        if (dto.getVendorCallId() != null) {
//            builder.setVendorCallId(dto.getVendorCallId());
//        }
//        if (dto.getVendorName() != null) {
//            builder.setVendorName(dto.getVendorName());
//        }
//        if (dto.getVendorAccountId() != null) {
//            builder.setVendorAccountId(dto.getVendorAccountId());
//        }
//        if (dto.getEventMetadata() != null) {
//            builder.putAllEventMetadata(dto.getEventMetadata());
//        }
//        if (dto.getObserveAccountId() != null) {
//            builder.setObserveAccountId(dto.getObserveAccountId());
//        }
//        if (dto.getObserveUserId() != null) {
//            builder.setObserveUserId(dto.getObserveUserId());
//        }
//        if (dto.getVendorAgentId() != null) {
//            builder.setVendorAgentId(dto.getVendorAgentId());
//        }
//
//        return builder.build();
//    }
}
