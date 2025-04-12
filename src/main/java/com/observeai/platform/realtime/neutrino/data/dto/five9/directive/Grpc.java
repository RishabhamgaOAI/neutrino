package com.observeai.platform.realtime.neutrino.data.dto.five9.directive;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Grpc {
    private String streamFor;
    private String targetUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grpc grpc = (Grpc) o;
        return Objects.equals(streamFor, grpc.streamFor) && Objects.equals(targetUrl, grpc.targetUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamFor, targetUrl);
    }
}
