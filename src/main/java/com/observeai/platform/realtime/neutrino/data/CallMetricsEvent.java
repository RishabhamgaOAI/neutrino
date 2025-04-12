package com.observeai.platform.realtime.neutrino.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class CallMetricsEvent {
    private String observeCallId;
    private String observeAccountId;
    private String observeUserId;
    private String vendorCallId;
    private String vendor;
    private String vendorAccountId;
    private String vendorUserId;
    private Long callStartMetaEventTs;
    private Long callStreamTs;
    private String detail;

    public Map<String, Object> toMap() {
        Map<String, Object> res = new HashMap<>();
        res.put("observe_call_id", observeCallId);
        res.put("observe_account_id", observeAccountId);
        res.put("observe_user_id", observeUserId);
        res.put("vendor_call_id", vendorCallId);
        res.put("vendor", vendor);
        res.put("vendor_account_id", vendorAccountId);
        res.put("vendor_user_id", vendorUserId);
        res.put("call_start_meta_event_ts", callStartMetaEventTs);
        res.put("call_stream_ts", callStreamTs);
        res.put("detail", detail);
        return res;
    }
}
