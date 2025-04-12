package com.observeai.platform.realtime.neutrino.data.nice;


import com.observeai.platform.realtime.neutrino.util.ObjectMapperFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newrelic.api.agent.NewRelic;
import java.util.HashMap;

import static com.observeai.platform.realtime.neutrino.util.Constants.APP_PARAMS;

@Slf4j
@Data
@AllArgsConstructor
@Builder
public class NiceInitializeMessage {
    private String vendorAccountId;
    private String agentId;
    private String vendorCallId;
    private String direction;
    private String track;
    private String additionalParams;

    // Field to store parsed additionalParams
    private Map<String, String> additionalParamsMap;

    private static final ObjectMapper objectMapper = ObjectMapperFactory.getPascalCaseObjectMapper();
    // Default no-argument constructor
    public NiceInitializeMessage() {}
    
    public static NiceInitializeMessage fromJsonMessage(JSONObject message) {
        String accountId, agentId, vendorCallId, additionalParams;
        String direction, track;
        int streamPerspective;

        /* Remove the additional '\r\n' characters sent along with the custom call metadata */
        String appParams = message.getString(APP_PARAMS).replaceAll("\r\n","");
        JSONObject appParamsJsonObject = new JSONObject(appParams);
        accountId = appParamsJsonObject.getString("accountId");
        agentId = appParamsJsonObject.getString("agentId");
        direction = appParamsJsonObject.getString("callDirection");
        vendorCallId = appParamsJsonObject.getString("contactId");
        additionalParams = appParamsJsonObject.optString("additionalParams", null);

        // Parse additionalParams JSON into a Map
        Map<String, String> additionalParamsMap = new HashMap<>();
        if (additionalParams != null && !additionalParams.isEmpty()) {
            try {
                additionalParamsMap = objectMapper.readValue(additionalParams, Map.class);
            } catch (JsonProcessingException e) {
                NewRelic.noticeError(e);
                log.error("error parsing additionalParams=", additionalParams, e);
            }
        } 

        streamPerspective = message.getInt("streamPerspective");
        track = streamPerspective == 0 ? "inbound" : "outbound";
        
        return new NiceInitializeMessage(
            accountId,
            agentId,
            vendorCallId,
            direction,
            track,
            additionalParams,
            additionalParamsMap // Set the parsed map
        );
    }
}
