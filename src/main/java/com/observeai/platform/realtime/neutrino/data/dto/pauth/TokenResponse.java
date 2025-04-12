package com.observeai.platform.realtime.neutrino.data.dto.pauth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TokenResponse {
    private String partnerUid;
    private String tokenType;
    private String accessToken;
    private String region;
}
