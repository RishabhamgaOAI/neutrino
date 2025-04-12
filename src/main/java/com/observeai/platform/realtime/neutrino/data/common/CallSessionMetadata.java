package com.observeai.platform.realtime.neutrino.data.common;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CallSessionMetadata {
    String callSourceName;
    String socketAddress;
    boolean callWatchCall = false;

    public CallSessionMetadata(String socketAddress) {
        this.socketAddress = socketAddress;
    }
}
