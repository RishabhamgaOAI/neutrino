package com.observeai.platform.realtime.neutrino.data.ccclogic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import static com.observeai.platform.realtime.neutrino.util.Constants.*;

@Slf4j
@Data
@AllArgsConstructor
@Builder
public class StartMessage {
    private String vendorAccountId;
    private String agentId;
    private String vendorCallId;
    private String direction;

    public static StartMessage fromJsonMessage(JSONObject message) {
        JSONObject start = message.getJSONObject(START_MESSAGE);
        String accountId = start.getString(ACCOUNT_SID);
        String vendorCallId = start.getString(CALL_SID);

        JSONObject customParams = start.optJSONObject(TALKDESK_CUSTOM_PARAMETERS);
        String agentId = customParams.getString(AGENT_ID);
        String direction = customParams.getString(CALL_DIRECTION);

        return new StartMessage(accountId, agentId, vendorCallId, direction);
    }
}
