package com.observeai.platform.realtime.neutrino.data.dto.five9.directive;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sip {
    private String streamFor;
    private List<String> targetUri;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sip sip = (Sip) o;
        return Objects.equals(streamFor, sip.streamFor) && Objects.equals(targetUri, sip.targetUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamFor, targetUri);
    }
}
