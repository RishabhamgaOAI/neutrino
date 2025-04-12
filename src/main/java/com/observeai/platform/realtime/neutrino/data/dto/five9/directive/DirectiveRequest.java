package com.observeai.platform.realtime.neutrino.data.dto.five9.directive;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DirectiveRequest {
    private String trustToken;
    private Grpc grpc;
    private String callEventUrl;
    private String voicestreamEventUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectiveRequest that = (DirectiveRequest) o;
        return Objects.equals(trustToken, that.trustToken) && Objects.equals(grpc, that.grpc) && Objects.equals(callEventUrl, that.callEventUrl) && Objects.equals(voicestreamEventUrl, that.voicestreamEventUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trustToken, grpc, callEventUrl, voicestreamEventUrl);
    }
}
