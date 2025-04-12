package com.observeai.platform.realtime.neutrino.context;

public enum MdcFieldNames {
    OBSERVE_CALL_ID("observeCallId"),
    SECONDARY_CALL_ID("secondaryCallId"),
    SESSION_ID("sessionId"),

    OBSERVE_ACCOUNT_ID("observeAccountId"),
    OBSERVE_USER_ID("observeUserId"),

    VENDOR("vendor"),
    VENDOR_ACCOUNT_ID("vendorAccountId"),
    VENDOR_CALL_ID("vendorCallId"),
    VENDOR_USER_ID("vendorUserId"),

    CALL_BACK_META_EVENT_TYPE("callBackMetaEventType");

    private final String value;

    MdcFieldNames(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
