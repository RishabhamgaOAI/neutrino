package com.observeai.platform.realtime.neutrino.data.common;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.observeai.platform.realtime.neutrino.enums.MomentTheme;
import com.observeai.platform.realtime.neutrino.enums.Speaker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BaseMoment {
    public String id;
    public String name;
    public String modelName;
    public MomentTheme theme;
    public Speaker speaker;
}
