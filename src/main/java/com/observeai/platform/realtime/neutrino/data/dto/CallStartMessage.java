package com.observeai.platform.realtime.neutrino.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import com.observeai.platform.realtime.neutrino.enums.Speaker;
import lombok.*;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import static com.observeai.platform.realtime.neutrino.service.impl.TalkdeskEventHandlerServiceImpl.TALKDESK_VENDOR_NAME;
import static com.observeai.platform.realtime.neutrino.util.Constants.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CallStartMessage {
    private String vendor;
    private String accountId;
    private String agentId;
    private String vendorCallId;
    private String partnerMeetingId;
    private boolean recordAudio;
    private boolean supervisorAssistAudioEnabled;
    private boolean isPci;
    private boolean reconnectionAllowed;
    private String deploymentCluster;
    private CallDirection direction;
    private Speaker speaker;
    private boolean previewCall = false;
    private String experienceId;
    private String observeCallId;

    public CallStartMessage(String vendor, String accountId, String agentId, String vendorCallId, String partnerMeetingId, 
        boolean recordAudio, boolean supervisorAssistAudioEnabled, boolean isPci, boolean reconnectionAllowed, 
        String deploymentCluster, CallDirection direction, Speaker speaker, String observeCallId) {
        this(vendor, accountId, agentId, vendorCallId, partnerMeetingId, recordAudio, supervisorAssistAudioEnabled, 
        isPci, reconnectionAllowed, deploymentCluster, direction, speaker, false, null, observeCallId);
    }

    public CallStartMessage(AccountInfoWithVendorDetailsDto accountInfo, String observeCallId,String vendorCallId, CallDirection direction) {
        this(accountInfo, null, observeCallId, vendorCallId, direction);
    }

    public CallStartMessage(AccountInfoWithVendorDetailsDto accountInfo, String observeUserId, String observeCallId, String vendorCallId, CallDirection direction) {
        this(accountInfo.getVendorAccountDetails().getVendor(), accountInfo.getObserveAccountId(), observeUserId, vendorCallId, vendorCallId,
        accountInfo.isRecordAudio(), isSupervisorAssistAudioEnabled(accountInfo), accountInfo.isPci(), false, 
        accountInfo.getDeploymentCluster(), direction, null, observeCallId);
    }

    public static CallStartMessage buildChildCallStartMessage(CallStartMessage other, String observeCallId) {
        return new CallStartMessage(other.getVendor(), other.getAccountId(), null, other.getVendorCallId(), other.getPartnerMeetingId(), 
        other.isRecordAudio(), other.isSupervisorAssistAudioEnabled(), other.isPci(), other.isReconnectionAllowed(), 
        other.getDeploymentCluster(), other.getDirection(), other.getSpeaker(), other.isPreviewCall(), other.getExperienceId(), observeCallId);
    }

    private static boolean isSupervisorAssistAudioEnabled(AccountInfoWithVendorDetailsDto accountInfoWithVendorDetailsDto) {
        if(accountInfoWithVendorDetailsDto.getSupervisorAssistProperties() == null)
            return false;
        return accountInfoWithVendorDetailsDto.getSupervisorAssistProperties().getAudioEnabled();
    }

    public String details() {
        return String.format("vendorCallId: %s, agentId: %s, accountId: %s", vendorCallId, agentId, accountId);
    }

    public static CallStartMessage fromJsonMessage(JSONObject start, String observeCallId) {
        CallDirection direction;
        Speaker speaker = null;
        String accountId, vendorCallId, partnerMeetingId, vendor = null;
        JSONObject customParams = start.optJSONObject(TALKDESK_CUSTOM_PARAMETERS);
        if (customParams != null && !customParams.isEmpty()) {
            accountId = customParams.getString(ACCOUNT_ID);
            vendorCallId = customParams.getString(INTERACTION_ID);
            partnerMeetingId = customParams.optString(PARTNER_MEETING_ID_SNAKE_CASE, null);
            direction = CallDirection.from(customParams.getString(CALL_TYPE));
            vendor = TALKDESK_VENDOR_NAME;
        } else {
            accountId = start.getString(ACCOUNT_SID);
            vendorCallId = start.getString(CALL_SID);
            partnerMeetingId = start.optString(PARTNER_MEETING_ID_CAMEL_CASE, null);
            speaker = Speaker.from(start);
            direction = CallDirection.from(start);
            vendor = start.optString(VENDOR, null);
        }
        String agentId = start.optString(AGENT_SID);
        boolean recordAudio = start.optBoolean(RECORD_AUDIO);
        boolean supervisorAssistAudioEnabled = start.optBoolean(SUPERVISOR_ASSIST_AUDIO_ENABLED);
        boolean isPci = start.optBoolean(IS_PCI);
        boolean reconnectionAllowed = start.optBoolean(RECONNECTION_ALLOWED);
        String deploymentCluster = start.optString(DEPLOYMENT_CLUSTER);
        boolean previewCall = start.optBoolean(PREVIEW_CALL);
        String experienceId = start.optString(EXPERIENCE_ID);
        return new CallStartMessage(vendor, accountId, agentId, vendorCallId, partnerMeetingId, recordAudio, supervisorAssistAudioEnabled, isPci, reconnectionAllowed, deploymentCluster,direction, speaker, previewCall, experienceId, observeCallId);
    }

    public static CallStartMessage fromCallEventDto(CallEventDto callEventDto) {
        return new CallStartMessage(callEventDto.getVendor(), callEventDto.getObserveAccountId(),
                callEventDto.getObserveUserId(), callEventDto.getVendorCallId(), callEventDto.getPartnerMeetingId(),
                callEventDto.isRecordAudio(), callEventDto.isSupervisorAssistAudioEnabled(), callEventDto.isPci(),
                callEventDto.isReconnectionAllowed(), callEventDto.getDeploymentCluster(), callEventDto.getCallDirection(),
                null, callEventDto.isPreviewCall(), callEventDto.getExperienceId(), callEventDto.getObserveCallId());
    }

    @JsonIgnore
    public boolean hasDirection() {
        return (direction != null);
    }

    @JsonIgnore
    public boolean isComplete() {
        return (StringUtils.hasLength(accountId) && StringUtils.hasLength(agentId) && hasDirection());
    }

    @JsonIgnore
    public void setDirectionOrDefault(CallDirection direction, CallDirection defaultDirection) {
        this.direction = (direction == null ? defaultDirection : direction);
    }
}
