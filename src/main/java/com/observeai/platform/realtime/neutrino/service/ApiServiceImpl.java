package com.observeai.platform.realtime.neutrino.service;

import com.observeai.platform.realtime.neutrino.auth.AuthenticationService;
import com.observeai.platform.realtime.neutrino.data.dto.AccountAndUserInfoResponseDto;
import com.observeai.platform.realtime.neutrino.data.dto.NiceEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.TalkdeskEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.TwilioEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static com.observeai.platform.realtime.neutrino.util.Constants.*;
import static com.observeai.platform.realtime.neutrino.util.TwilioConstants.*;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ApiServiceImpl implements ApiService {

    private final TwilioEventHandlerService twilioEventHandlerService;
    private final TalkdeskEventHandlerService talkdeskEventHandlerService;
    private final NiceEventHandlerService niceEventHandlerService;
    private final AuthenticationService authenticationService;

    @Override
    public ResponseEntity<Object> handleTwilioEvent(TwilioEventDto twilioEventDto) {
        switch (twilioEventDto.getEventType()) {
            case RESERVATION_CREATED:
                twilioEventHandlerService.handleCallStartEvent(twilioEventDto);
                break;
            case RESERVATION_WRAPUP:
            case RESERVATION_COMPLETED:
            case RESERVATION_CANCELLED:
                twilioEventHandlerService.handleCallEndEvent(twilioEventDto);
                break;
            default:
                log.info("Ignoring twilio event for accountId={} with eventType={}", twilioEventDto.getAccountSid(), twilioEventDto.getEventType());
        }
        return ResponseEntity.noContent().header("Content-Type", "application/json").build();
    }

    @Override
    public ResponseEntity<Object> handleTalkdeskEvent(TalkdeskEventDto talkdeskEventDto) {
        switch (talkdeskEventDto.getEventType()) {
            case CALL_ANSWERED:
                talkdeskEventHandlerService.handleCallStartEvent(talkdeskEventDto);
                break;
            case CALL_FINISHED:
                talkdeskEventHandlerService.handleCallEndEvent(talkdeskEventDto);
                break;
            default:
                log.info("Ignoring talkdesk event for accountId={} with eventType={}", talkdeskEventDto.getAccountId(), talkdeskEventDto.getEventType());
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Object> handleNiceEvent(NiceEventDto niceEventDto) {
        try {
            log.info("received NICE call back event of type: {} for accountId={}, agentId={}, callId={}", niceEventDto.getEvent(), niceEventDto.getAccountId(), niceEventDto.getAgentId(), niceEventDto.getContactId());
            AccountAndUserInfoResponseDto accountAndUserInfo = niceEventHandlerService.getAccountAndUserInfo(niceEventDto.getContactId(), niceEventDto.getAccountId(), niceEventDto.getAgentId());
            if(accountAndUserInfo != null) {
                boolean authenticated = authenticationService.authenticateNiceEvent(niceEventDto, accountAndUserInfo);
                if(!authenticated){
                    return ResponseEntity.status(401).build();
                }
            }
            switch (niceEventDto.getEvent()) {
                case CALL_START:
                    niceEventHandlerService.handleCallStartEvent(niceEventDto, accountAndUserInfo);
                    break;
                case CALL_HOLD:
                    niceEventHandlerService.handleCallHoldEvent(niceEventDto, accountAndUserInfo);
                    break;
                case CALL_RESUME:
                    niceEventHandlerService.handleCallResumeEvent(niceEventDto, accountAndUserInfo);
                    break;
                case CALL_END:
                    niceEventHandlerService.handleCallEndEvent(niceEventDto, accountAndUserInfo);
                    break;
                default:
                    log.info("Ignoring NICE event for accountId={} with eventType={}", niceEventDto.getAccountId(), niceEventDto.getEvent());
            }
        } catch (Throwable th) {
            log.error("error while handling NICE event={}, error={}", niceEventDto.toString(), th.toString(), th);
        }
        return ResponseEntity.ok().build();
    }

}
