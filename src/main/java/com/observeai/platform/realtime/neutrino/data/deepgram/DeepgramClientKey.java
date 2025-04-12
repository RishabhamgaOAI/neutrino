package com.observeai.platform.realtime.neutrino.data.deepgram;

import com.observeai.platform.realtime.neutrino.config.CallSourceConfig;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class DeepgramClientKey {
	private final boolean onPrem;
	private final CallSourceConfig callSourceConfig;
}
