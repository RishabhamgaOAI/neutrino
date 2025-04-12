package com.observeai.platform.realtime.neutrino.data.genesys;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientMessageBase extends MessageBase {
    private long serverseq;
    private String duration;
}
