package com.observeai.platform.realtime.neutrino.util;

import com.observeai.platform.realtime.neutrino.client.DeepgramClient;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SessionMetadata {
    private DeepgramClient deepgramClient;
    private String status;
    private String username;
    private String callId;
}
