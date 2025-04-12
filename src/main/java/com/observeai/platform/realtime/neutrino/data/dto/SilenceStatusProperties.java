package com.observeai.platform.realtime.neutrino.data.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SilenceStatusProperties {
    private Integer timeLimit;
    private String name;
    private String description;
}
