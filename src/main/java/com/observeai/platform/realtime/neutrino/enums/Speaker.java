package com.observeai.platform.realtime.neutrino.enums;

import com.observeai.platform.realtime.neutrino.util.Constants;
import org.json.JSONObject;

public enum Speaker {
    AGENT ( "agent"),
    CUSTOMER ("customer"),
    BOTH ("both");

    private final String value;

    Speaker(String value) {
        this.value = value;
    }

    public static Speaker from(String value) {
        for (Speaker speaker : values()) {
            if (speaker.value.equals(value.toLowerCase())) {
                return speaker;
            }
        }
        return null;
    }

    public static Speaker from(JSONObject object) {
        if (!object.has(Constants.SPEAKER))
            return null;
        return from(object.getString(Constants.SPEAKER));
    }
}
