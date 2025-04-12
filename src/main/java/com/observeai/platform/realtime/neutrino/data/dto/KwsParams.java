package com.observeai.platform.realtime.neutrino.data.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kws.default")
@Data
@JsonNaming(SnakeCaseStrategy.class)
public class KwsParams {
    private int maximumThreshold;
    private double windowToKeywordSizeFactor;
    private int smallestWindowSize;
}
