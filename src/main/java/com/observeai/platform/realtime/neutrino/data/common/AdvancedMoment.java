package com.observeai.platform.realtime.neutrino.data.common;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdvancedMoment extends Moment {
    private Equation equation;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Equation {
        private String operator;
        private String value;
        private List<Equation> operants;
        private Map<String, String> groupDetails;
    }
}
