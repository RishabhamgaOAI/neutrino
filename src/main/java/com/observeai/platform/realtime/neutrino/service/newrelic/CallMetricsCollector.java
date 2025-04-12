package com.observeai.platform.realtime.neutrino.service.newrelic;

import com.newrelic.api.agent.NewRelic;
import com.observeai.platform.realtime.neutrino.config.CallMetricsConfig;
import com.observeai.platform.realtime.neutrino.data.CallMetricsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallMetricsCollector {
    private static final String CALL_METRICS_EVENT = "rt_call_stream_and_event_metrics";
    private final CallMetricsConfig callMetricsConfig;

    public void reportCallMetricsEvent(CallMetricsEvent callMetricsEvent) {
        if (!callMetricsConfig.isEnabled())
            return;
        try {
            Map<String, Object> event = callMetricsEvent.toMap();
            log.info("Pushing call stream/event metrics {} to New Relic", event);
            NewRelic.getAgent().getInsights().recordCustomEvent(CALL_METRICS_EVENT, event);
        } catch (Throwable th) {
            log.error("Failed to report missing call stream to NewRelic with exception", th);
        }
    }
}
