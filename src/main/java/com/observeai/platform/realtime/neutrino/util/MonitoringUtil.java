package com.observeai.platform.realtime.neutrino.util;

import com.observeai.platform.integration.commons.monitoring.MonitoringParams;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallTopicEventNotification;
import com.observeai.platform.realtime.neutrino.data.Call;

import java.util.*;

import static com.observeai.platform.realtime.neutrino.util.Constants.*;

public class MonitoringUtil {

    private static final EnumSet<CallEventType> END_EVENTS = EnumSet.of(CallEventType.END_EVENT, CallEventType.MONITORING_END_EVENT);
    
    public static Map<MonitoringParams, Object> createCallNotificationMonitoringParams(Call call, CallTopicEventNotification callTopicEventNotification, CallEventType callEventtype) {
        Map<MonitoringParams, Object> resultMap = new HashMap<>();
        putIfNonNull(resultMap, MonitoringParams.ACCOUNT_ID, callTopicEventNotification.getAccountId());
        putIfNonNull(resultMap, MonitoringParams.USER_ID, callTopicEventNotification.getUserId());
        putIfNonNull(resultMap, MonitoringParams.VENDOR, call.getVendor());
        putIfNonNull(resultMap, MonitoringParams.VENDOR_CALL_ID, call.getStartMessage().getVendorCallId());
        putIfNonNull(resultMap, MonitoringParams.CALL_ID, call.getObserveCallId());
        putIfNonNull(resultMap, MonitoringParams.ACCOUNT_NAME, call.getAccountName());
        putIfNonNull(resultMap, MonitoringParams.NOTIFICATION_ID, callTopicEventNotification.getNotificationId());
        putIfNonNull(resultMap, MonitoringParams.EVENT_NAME, getCallEventTypeString(callEventtype));
        putIfNonNull(resultMap, MonitoringParams.BE_TRIGGER_TIMESTAMP, callTopicEventNotification.getMessage().getTime());
        putIfNonNull(resultMap, MonitoringParams.EVENT_TIME, System.currentTimeMillis());
        putIfNonNull(resultMap, MonitoringParams.ORIGIN, BACKEND);
        putIfNonNull(resultMap, MonitoringParams.FEATURE_NAME, CALL_EVENT);
        if (END_EVENTS.contains(callEventtype)) {
            putIfNonNull(resultMap, MonitoringParams.CALL_DURATION, callTopicEventNotification.getMessage().getTime() - call.getStartTime());
        }

        return resultMap;

    }
    private static void putIfNonNull(Map<MonitoringParams, Object> map, MonitoringParams key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    public static String getCallEventTypeString(CallEventType callEventType) {
        switch (callEventType) {
            case START_EVENT:
                return CALL_START;
            case END_EVENT:
                return CALL_END;
            case MONITORING_START_EVENT:
                return MONITORING_CALL_START;
            case MONITORING_END_EVENT:
                return MONITORING_CALL_END;
            default:
                throw new IllegalArgumentException("Unknown CallEventType: " + callEventType);
        }
    }
}
