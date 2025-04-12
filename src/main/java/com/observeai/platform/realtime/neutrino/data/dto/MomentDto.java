package com.observeai.platform.realtime.neutrino.data.dto;

import com.observeai.platform.realtime.neutrino.data.common.AdvancedMoment;
import com.observeai.platform.realtime.neutrino.data.common.Moment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MomentDto {
    private String id;
    private String name;

    public static MomentDto fromMoment(Moment moment) {
        return new MomentDto(moment.getId(), moment.getName());
    }

    public static MomentDto fromAdvancedMoment(AdvancedMoment moment) {
        return new MomentDto(moment.getId(), moment.getName());
    }

    public static List<MomentDto> fromMoments(List<Moment> moments) {
        return moments.stream().map(MomentDto::fromMoment).collect(Collectors.toList());
    }

    public static List<MomentDto> fromAdvancedMoments(List<AdvancedMoment> moments) {
        return moments.stream().map(MomentDto::fromAdvancedMoment).collect(Collectors.toList());
    }
}
