package com.observeai.platform.realtime.neutrino.data.genesys;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ServerMessageBase extends MessageBase {
    private long clientseq;

    public ServerMessageBase(String id, String type, long seq, Map<String, Object> parameters, long clientseq) {
        super(id, type, seq, parameters);
        this.clientseq = clientseq;
    }
}
