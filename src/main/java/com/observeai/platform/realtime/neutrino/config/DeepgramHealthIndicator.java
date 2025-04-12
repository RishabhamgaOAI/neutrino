package com.observeai.platform.realtime.neutrino.config;

import com.newrelic.api.agent.NewRelic;
import com.observeai.platform.realtime.neutrino.client.DeepgramProperties;
import com.observeai.platform.realtime.neutrino.client.SlackClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DeepgramHealthIndicator implements HealthIndicator {
    private static int count = 0;
    private static String NEWRELIC_DG_STATUS_REPORT_NAME = "rt_deepgram_status";
    private final RestTemplate restTemplate;
    private final DeepgramProperties deepgramProperties;
    private final SlackClient slackClient;
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(5);
    private boolean flag = false;

    @Override
    public Health health() {
        try {
            URI deepgramUri = UriComponentsBuilder.newInstance()
                    //.scheme(deepgramProperties.getHttpScheme())
                    .host(deepgramProperties.getHost())
                    //.path(deepgramProperties.getHealthPath())
                    .build().toUri();
            ResponseEntity<String> entity = restTemplate
                    .getForEntity(deepgramUri, String.class);
            if (count >= 5 && flag) {
                flag = false;
                slackClient.sendMessage(DateTime.now().toString() + ": deepgram is back to normal");
            }
            if (count < 5)
                count++;
            reportStatusNR(Status.UP);
            return Health.up().build();
        } catch (Exception e) {
            count = 0;
            flag = true;
            log.error("========== deepgram is down ==========");
            slackClient.sendMessage(DateTime.now().toString() + ": deepgram health is down");
        }
        reportStatusNR(Status.DOWN);
        return Health.down().build();
    }

    private void reportStatusNR(Status status) {
        asyncExecutor.execute(() -> {
            try {
                NewRelic.getAgent().getInsights().recordCustomEvent(
                        NEWRELIC_DG_STATUS_REPORT_NAME, Collections.singletonMap("STATUS", status.getCode()));
            } catch (Throwable th) {
                log.error("Failed to publish health status for DG liveness", th);
            }
        });
    }
}
