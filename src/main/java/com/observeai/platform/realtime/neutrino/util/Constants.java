package com.observeai.platform.realtime.neutrino.util;

public class Constants {
    public static final String AUTH_CONTEXT = "authContext";
    public static final String BASIC_AUTH_PREFIX = "Basic ";
    public static final String TOKEN_AUTH_PREFIX = "Token ";
    public static final String INTERNAL_SERVER_ERROR = "internal_server_error";
    public static final String VENDOR = "vendor";
    public static final String ACCOUNT_SID = "accountSid";
    public static final String CALL_SID = "callSid";
    public static final String AGENT_SID = "agentSid";
    public static final String START_MESSAGE = "start";
    public static final String RECORD_AUDIO = "recordAudio";
    public static final String SUPERVISOR_ASSIST_AUDIO_ENABLED = "supervisorAssistAudioEnabled";
    public static final String METADATA_BASED_SCRIPTS_ENABLED = "metadataBasedScriptsEnabled";
    public static final String IS_PCI = "isPci";
    public static final String RECONNECTION_ALLOWED = "reconnectionAllowed";
    public static final String DEPLOYMENT_CLUSTER = "deploymentCluster";
    public static final String TRACK = "track";
    public static final String MEDIA = "media";
    public static final String MEDIA_FORMAT = "mediaFormat";
    public static final String STEREO = "stereo";
    public static final String DIRECTION = "direction";
    public static final String INBOUND = "inbound";
    public static final String OUTBOUND = "outbound";
    public static final String SPEAKER = "speaker";
    public static final String BILL = "BILL";
    public static final String DEFAULT = "DEFAULT";
    public static final String BACKEND = "BACKEND";
    public static final String CALL_EVENT = "CALL_EVENT";
    public static final String SIGNATURE_ALGORITHM = "HmacSHA256";

    /*Additional constants for TalkDesk*/
    public static final String TALKDESK_CUSTOM_PARAMETERS = "customParameters";
    public static final String INTERACTION_ID = "interaction_id";
    public static final String PARTNER_MEETING_ID_SNAKE_CASE = "partner_meeting_id";
    public static final String PARTNER_MEETING_ID_CAMEL_CASE = "partnerMeetingId";
    public static final String ACCOUNT_ID = "account_id";
    public static final String CALL_TYPE = "type";
    public static final String CALL_ANSWERED = "call_answered";
    public static final String CALL_FINISHED = "call_finished";
    public static final String CALL_START = "CALL_START";
    public static final String CALL_HOLD = "CALL_HOLD";
    public static final String CALL_RESUME = "CALL_RESUME";
    public static final String CALL_END = "CALL_END";
    public static final String CALL_ID = "call_id";
    public static final String MONITORING_CALL_START = "MONITORING_CALL_START";
    public static final String MONITORING_CALL_END = "MONITORING_CALL_END";

    /*Additional constants for NICE inContact*/
    public static final String AGENT_ID = "agentId";
    public static final String CONTACT_ID = "contactId";
    public static final String BUSINESS_UNIT = "busNo";
    public static final String CALL_DIRECTION = "callDirection";
    public static final String APP_PARAMS = "appParams";
    public static final String EXECUTION_INFO = "executionInfo";
    public static final String CALL_HANDLE = "callHandle";

    public static final String UNKNOWN = "unknown";
    public static final String SEMI_COLON = ";";
    public static final String START_MESSAGES = "call-start-messages";
    public static final String CALL_STATUS = "call-status";
    public static final String CALL = "call";
    public static final String CALL_BACK_META_EVENTS = "call-back-meta-events";
    public static final String CALL_METADATA = "call-metadata";
    public static final String MOMENTS = "moments";
    public static final String CALL_NOTES = "call-notes";
    public static final String CALL_EVENTS = "call-events";
    public static final String CALL_ID_PREFIX = "rt-call-";
    public static final String MESSAGE_TYPE = "type";
    public static final String LIVE_CALLS_SUFFIX = "live-calls";
    public static final String VENDOR_CALL_ID = "vendor_call_id";
    public static final String EQUALS = "=";
    public static final String USER_ID = "user_id";
    public static final String HOLD_ML_MODEL_NAME = "silence-hold-time-violation";
    public static final String DEADAIR_ML_MODEL_NAME = "silence-deadair";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PREVIEW_CALL = "previewCall";
    public static final String EXPERIENCE_ID ="experienceId";
    public static final String FIVE9 ="FIVE9";
    public static final String NICE ="NICE";
    public static final String AVAYA = "AVAYA";

    public static class CallSourceNameConstants {
        public static final String TWILIO_CALL_SOURCE_NAME = "twilio";
        public static final String OBSERVE_AUDIO_CAPTURE_CALL_SOURCE_NAME = "observe-audio-capture";
        public static final String FIVE9_CALL_SOURCE_NAME = "five9";
        public static final String NICE_CALL_SOURCE_NAME = "nice";
        public static final String CCCLOGIC_CALL_SOURCE_NAME = "3clogic";
        public static final String ZOOMCC_CALL_SOURCE_NAME = "zoom-cc";
        public static final String AWS_CONNECT_CALL_SOURCE_NAME = "aws-connect";
        public static final String GENESYS_CALL_SOURCE_NAME = "genesys";
    }

    public static class CallStreamerConstants {
        public static final String START_ANCHOR_MOMENT_DETECTED = "START_ANCHOR_MOMENT_DETECTED";
        public static final String END_ANCHOR_MOMENT_DETECTED = "END_ANCHOR_MOMENT_DETECTED";
        public static final String MANUAL_CALL_START = "MANUAL_CALL_START";
        public static final String MANUAL_CALL_END = "MANUAL_CALL_END";
    }
}
