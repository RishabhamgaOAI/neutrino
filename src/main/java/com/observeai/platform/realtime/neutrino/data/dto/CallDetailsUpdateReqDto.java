package com.observeai.platform.realtime.neutrino.data.dto;

import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallDetailsUpdateReqDto {
    private CallDirection direction;
}
