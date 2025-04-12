package com.observeai.platform.realtime.neutrino.data.genesys;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class MessageBase {
    private String version = "2";
    private String id;
    private String type;
    private long seq;
    private Map<String, Object> parameters;

    public MessageBase(String id, String type, long seq, Map<String, Object> parameters) {
        this.id = id;
        this.type = type;
        this.seq = seq;
        this.parameters = parameters;
    }
}
