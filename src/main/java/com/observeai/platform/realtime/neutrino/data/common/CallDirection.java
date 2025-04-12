package com.observeai.platform.realtime.neutrino.data.common;

import com.observeai.platform.realtime.neutrino.util.Constants;
import lombok.Getter;
import org.json.JSONObject;

@Getter
public enum CallDirection {
    INBOUND(Constants.INBOUND),
    OUTBOUND(Constants.OUTBOUND),
    UNKNOWN(Constants.UNKNOWN);

    private final String value;

    CallDirection(String value) {
        this.value = value;
    }

    public static CallDirection from(String value) {
        for (CallDirection callDirection : values()) {
            if (callDirection.value.equals(value.toLowerCase())) {
                return callDirection;
            }
        }
        return CallDirection.UNKNOWN;
    }

    public static CallDirection from(JSONObject object) {
        if (!object.has(Constants.DIRECTION))
            return null;
        return from(object.getString(Constants.DIRECTION));
    }
}
