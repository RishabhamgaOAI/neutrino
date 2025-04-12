package com.observeai.platform.realtime.commons.data.messages.details;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CallEvent{
    private String eventType;
}
