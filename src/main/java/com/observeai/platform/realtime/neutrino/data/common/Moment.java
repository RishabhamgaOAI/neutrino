package com.observeai.platform.realtime.neutrino.data.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Moment extends BaseMoment {
    private String description;
    @JsonProperty("expected")
    private boolean expected;
    private boolean sendAlert;
    private String suggestion;
    private List<String> keywords;
    private Location location;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Moment moment = (Moment) o;
        return Objects.equals(id, moment.id) && Objects.equals(name, moment.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Location {
        private String id;
        private long start;
        private long end;
        private String type;
        private String direction;
    }
}
