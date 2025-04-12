package com.observeai.platform.realtime.neutrino.data.dto;

import com.observeai.platform.realtime.neutrino.data.common.AdvancedMoment;
import com.observeai.platform.realtime.neutrino.data.common.Moment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountCriteriaResponseDto {
  private String accountId;
  private List<Moment> moments;
  private List<AdvancedMoment> advancedMoments;
}
