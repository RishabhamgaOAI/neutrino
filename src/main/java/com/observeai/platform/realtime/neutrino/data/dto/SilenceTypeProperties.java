package com.observeai.platform.realtime.neutrino.data.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class SilenceTypeProperties {
    private SilenceStatusProperties initiation;
    private SilenceStatusProperties violation;
}
