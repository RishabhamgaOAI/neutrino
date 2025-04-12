package com.observeai.platform.realtime.neutrino.service.newrelic;

import com.newrelic.api.agent.NewRelic;
import com.observeai.platform.realtime.neutrino.client.asr.deepgram.DeepgramClientPool;
import com.observeai.platform.realtime.neutrino.data.deepgram.DeepgramClientKey;
import com.observeai.platform.realtime.neutrino.data.store.CallRepository;
import com.observeai.platform.realtime.neutrino.handler.*;
import com.observeai.platform.realtime.neutrino.handler.CallHandler;
import com.observeai.platform.realtime.neutrino.handler.Five9Handler;
import com.observeai.platform.realtime.neutrino.redis.RedisCallsCounter;
import com.observeai.platform.realtime.neutrino.service.CallStateManager;
import com.observeai.platform.realtime.neutrino.service.DeepgramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.observeai.platform.realtime.neutrino.service.newrelic.MetricsCollectorConstants.*;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@EnableScheduling
public class MetricsCollector {

    private final CallHandler callHandler;
    private final Five9Handler five9Handler;
    private final GenesysPureCloudCallHandler genesysPureCloudCallHandler;
    private final CCCLogicCallHandler cccLogicCallHandler;
    private final NiceInContactCallHandler niceInContactCallHandler;
    private final CallRepository callRepository;
    private final CallStateManager callStateManager;
    private final RedisCallsCounter redisCallsCounter;
    private final DeepgramService deepgramService;
    private final Map<DeepgramClientKey, DeepgramClientPool> deepgramClientPools;

    @Scheduled(cron = "${newrelic.metricsCollectionCron}")
    public void reportInstanceLevelMetrics() {
        Map<String, Object> instanceLevelMetrics = new HashMap<>();
        instanceLevelMetrics.put(ACTIVE_CALLS_COUNT, callRepository.getCount());
        instanceLevelMetrics.put(ACTIVE_AUDIO_SESSIONS_COUNT, callHandler.getActiveSessions().size());
        instanceLevelMetrics.put(ACTIVE_FIVE9_AUDIO_SESSIONS_COUNT, five9Handler.getActiveSessions().size());
        instanceLevelMetrics.put(ACTIVE_GENESYS_AUDIO_SESSIONS_COUNT, genesysPureCloudCallHandler.getActiveSessions().size());
        instanceLevelMetrics.put(ACTIVE_3C_LOGIC_AUDIO_SESSIONS_COUNT, cccLogicCallHandler.getActiveSessions().size());
        instanceLevelMetrics.put(ACTIVE_NICE_AUDIO_SESSIONS_COUNT, niceInContactCallHandler.getActiveSessions().size());
        instanceLevelMetrics.put(ACTIVE_DEEPGRAM_SESSIONS_COUNT, deepgramService.getActiveDeepgramSessionsCount());
        for (DeepgramClientPool pool : deepgramClientPools.values()) {
            instanceLevelMetrics.put(pool.getName() + POOL_AVAILABLE_CLIENTS_COUNT_SUFFIX, pool.getClientsAvailable());
            instanceLevelMetrics.put(pool.getName() + POOL_USED_CLIENTS_COUNT_SUFFIX, pool.getClientsInUse());
        }
        reportMetricsAsEvent(NEUTRINO_INSTANCE_LEVEL_METRICS, instanceLevelMetrics);

    }

    @Scheduled(cron = "${newrelic.metricsCollectionCron}")
    public void reportServiceLevelMetrics() {
        Map<String, Object> serviceLevelMetrics = new HashMap<>();
        serviceLevelMetrics.put(ACTIVE_CALLS_COUNT, redisCallsCounter.get());
        reportMetricsAsEvent(NEUTRINO_SERVICE_LEVEL_METRICS, serviceLevelMetrics);
    }

    @Scheduled(cron = "${newrelic.metricsCollectionCron}")
    public void reportAbnormalCallCloseCounts() {
        callStateManager.getAndRefreshAbnormalCallCloseCounts().forEach((statusCode, closeCount) -> {
            if (closeCount > 0) {
                Map<String, Object> abnormalCallCloseCounts = new HashMap<>();
                abnormalCallCloseCounts.put(WS_CLOSE_STATUS_CODE, statusCode);
                abnormalCallCloseCounts.put(WS_CLOSE_COUNT, closeCount);
                reportMetricsAsEvent(ABNORMAL_CALL_CLOSE_COUNTS, abnormalCallCloseCounts);
            }
        });
    }

    public static void reportMetricsAsEvent(String eventName, Map<String, Object> metricsMap) {
        try {
            NewRelic.getAgent().getInsights().recordCustomEvent(eventName, metricsMap);
        } catch (Throwable th) {
            log.error("Failed to report metrics event={} to NewRelic with exception", eventName, th);
        }
    }
}
