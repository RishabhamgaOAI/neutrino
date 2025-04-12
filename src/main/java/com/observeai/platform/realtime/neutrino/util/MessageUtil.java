package com.observeai.platform.realtime.neutrino.util;

import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import com.observeai.platform.realtime.neutrino.data.dto.CallDetailsUpdateReqDto;
import org.json.JSONObject;
import org.springframework.web.socket.BinaryMessage;

public class MessageUtil {
    public static final String EVENT_KEY = "event";
    public static final String START_EVENT = "start";
    public static final String PING_EVENT = "ping";
    public static final String PONG_EVENT = "pong";
    public static final String UPDATE_EVENT = "update";
    public static final String MEDIA_EVENT = "media";
    public static final String TYPE_KEY = "type";
    public static final String SEQ_ID = "seq";
    public static final String CLIENT_TS_KEY = "clientTs";
    public static final String GATEWAY_TS_KEY = "gatewayTs";

    public static boolean isStartMessage(JSONObject jsonObject) {
        return jsonObject.getString(EVENT_KEY).equals(START_EVENT);
    }

    public static boolean isMediaMessage(JSONObject jsonObject) {
        return jsonObject.getString(EVENT_KEY).equals(MEDIA_EVENT);
    }

    public static boolean isUpdateMessage(JSONObject jsonObject) {
        return jsonObject.getString(EVENT_KEY).equals(UPDATE_EVENT);
    }

    public static boolean isInitializeMessage(JSONObject jsonObject) {
        return jsonObject.getJSONObject("executionInfo") != null;
    }

    public static JSONObject buildMediaMessageWithTrack(BinaryMessage message, String track) {
        JSONObject mediaMessage = new JSONObject(message.getPayload());
        mediaMessage.put(Constants.TRACK, track);
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put(Constants.MEDIA, mediaMessage);
        return jsonMessage;
    }

    public static CallDetailsUpdateReqDto getCallDetailsUpdateReqDto(JSONObject message) {
        JSONObject update = message.getJSONObject("update");
        CallDirection direction = CallDirection.from(update);
        return new CallDetailsUpdateReqDto(direction);
    }
}
