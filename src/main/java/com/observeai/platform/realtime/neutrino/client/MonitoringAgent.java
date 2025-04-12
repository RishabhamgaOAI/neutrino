package com.observeai.platform.realtime.neutrino.client;

import com.observeai.platform.integration.commons.monitoring.MonitorEventType;
import com.observeai.platform.integration.commons.monitoring.MonitoringParams;
import com.observeai.platform.integration.services.chitragupta.client.dto.MonitorEventDTO;
import com.observeai.platform.integration.services.chitragupta.client.service.iface.MonitoringServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.observeai.platform.realtime.neutrino.util.Constants.*;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MonitoringAgent {
    private final MonitoringServiceClient monitoringServiceClient;
    private final MonitoringServiceClient restrictedMonitoringServiceClient;

    public void sendMonitoringEvent(Map<MonitoringParams, Object> params, MonitorEventType monitorEventType, String cluster) throws Exception {
        MonitorEventDTO monitorEventDTO = createMonitorEventDTO(params, monitorEventType);
        monitorEventDTO.setId(UUID.randomUUID().toString());
        MonitoringServiceClient client = getMonitoringServiceClient(cluster);
        if (client != null) {
            client.sendMonitoringEvents(List.of(monitorEventDTO));
        } else {
            log.error("ObserveCallId: {}, Failed to send monitoring event for event : {}, Cluster not found for accountId: {}",params.get(MonitoringParams.CALL_ID), params.get(MonitoringParams.EVENT_NAME),  params.get(MonitoringParams.ACCOUNT_ID));
        }
    }

    private MonitorEventDTO createMonitorEventDTO(Map<MonitoringParams, Object> params, MonitorEventType monitorEventType) {
        return MonitorEventDTO.builder()
                .monitorEventType(monitorEventType)
                .params(params)
                .build();
    }

    private MonitoringServiceClient getMonitoringServiceClient(String cluster) {
        switch (cluster) {
            case BILL:
                return restrictedMonitoringServiceClient;
            case DEFAULT:
                return monitoringServiceClient;
            default:
                return null;
        }
    }
}