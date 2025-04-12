package com.observeai.platform.realtime.neutrino.data.dto;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;


@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MetadataBasedProperties{
    private boolean metadataBasedScriptsEnabled;
    private Long maxTagDetectionDelayInMs;
}
