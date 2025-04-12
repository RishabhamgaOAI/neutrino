package com.observeai.platform.realtime.neutrino.service.events;

import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.neutrino.config.CallMetricsConfig;
import com.observeai.platform.realtime.neutrino.data.CallMetricsEvent;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventType;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.redis.CallBackMetaEventsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallEventsRedisStore;
import com.observeai.platform.realtime.neutrino.redis.CallStartMessagesRedisStore;
import com.observeai.platform.realtime.neutrino.service.newrelic.CallMetricsCollector;
import com.observeai.platform.realtime.neutrino.util.CallEventJoinerUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.observeai.platform.realtime.neutrino.util.Constants.AVAYA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CallStartEventsJoinerTest {

    @Mock
    private CallEventsRedisStore callEventsRedisStore;

    @Mock
    private CallBackMetaEventsRedisStore callBackMetaEventsRedisStore;

    @Mock
    private CallStartMessagesRedisStore startMessagesRedisStore;

    @Mock
    private CallMetricsConfig callMetricsConfig;

    @Mock
    private CallMetricsCollector callMetricsCollector;

    @Mock
    private ScheduledExecutorService executorService;

    @InjectMocks
    private CallStartEventsJoiner callStartEventsJoiner;

    @BeforeEach
    public void setUp() {
        when(callMetricsConfig.getSchedulerThreadCount()).thenReturn(1);
        executorService= Executors.newScheduledThreadPool(callMetricsConfig.getSchedulerThreadCount());
    }

    @Test
    public void testGetVendorName_withNullValues() {
        String vendorName = callStartEventsJoiner.getVendorName(null, null);
        assertEquals("UNKNOWN", vendorName);
    }

    @Test
    public void testGetVendorName_withNullVendorNameInCallBackMetaEventDto() {
        CallBackMetaEventDto callBackMetaEventDto = new CallBackMetaEventDto();
        callBackMetaEventDto.setVendorName(null);

        String vendorName = callStartEventsJoiner.getVendorName(null, callBackMetaEventDto);
        assertEquals("UNKNOWN", vendorName);
    }

    @Test
    public void testGetVendorName_withNullVendorNameInCallBackMetaEventDtoAndNullVendorNameInCallEventDto() {
        CallBackMetaEventDto callBackMetaEventDto = new CallBackMetaEventDto();
        callBackMetaEventDto.setVendorName(null);
        CallEventDto callEventDto = new CallEventDto();
        callEventDto.setVendor(null);

        String vendorName = callStartEventsJoiner.getVendorName(callEventDto, callBackMetaEventDto);
        assertEquals("UNKNOWN", vendorName);
    }

    @Test
    public void testGetVendorName_withCallBackMetaEventDto() {
        CallBackMetaEventDto callBackMetaEventDto = new CallBackMetaEventDto();
        callBackMetaEventDto.setVendorName("VendorName");

        String vendorName = callStartEventsJoiner.getVendorName(null, callBackMetaEventDto);
        assertEquals("VendorName", vendorName);
    }

    @Test
    public void testGetVendorName_withNullVendorNameInCallBackMetaEventDtoAndVendorNameInCallEventDto() {
        CallBackMetaEventDto callBackMetaEventDto = new CallBackMetaEventDto();
        callBackMetaEventDto.setVendorName(null);
        CallEventDto callEventDto = new CallEventDto();
        callEventDto.setVendor("VendorName");

        String vendorName = callStartEventsJoiner.getVendorName(callEventDto, null);
        assertEquals("VendorName", vendorName);
    }

    @Test
    public void testUpdateCallDirection_withAvayaVendor() {
        CallEventDto callEventDto = new CallEventDto();
        callEventDto.setCallDirection(CallDirection.OUTBOUND);

        CallBackMetaEventDto callBackMetaEventDto = new CallBackMetaEventDto();
        callBackMetaEventDto.setVendorName(AVAYA);
        callBackMetaEventDto.setDirection(CallDirection.INBOUND);

        callStartEventsJoiner.updateCallDirection(callEventDto, callBackMetaEventDto);

        assertEquals(CallDirection.OUTBOUND, callEventDto.getCallDirection());
        callEventDto.setCallDirection(CallDirection.INBOUND);
        callStartEventsJoiner.updateCallDirection(callEventDto, callBackMetaEventDto);

        assertEquals(CallDirection.INBOUND, callEventDto.getCallDirection());
        callEventDto.setCallDirection(CallDirection.UNKNOWN);
        callStartEventsJoiner.updateCallDirection(callEventDto, callBackMetaEventDto);

        assertEquals(CallDirection.INBOUND, callEventDto.getCallDirection());
        callEventDto.setCallDirection(null);
        callStartEventsJoiner.updateCallDirection(callEventDto, callBackMetaEventDto);

        assertEquals(CallDirection.INBOUND, callEventDto.getCallDirection());
    }

    @Test
    public void testUpdateCallDirection_withNonAvayaVendor() {
        CallEventDto callEventDto = new CallEventDto();
        callEventDto.setCallDirection(CallDirection.OUTBOUND);

        CallBackMetaEventDto callBackMetaEventDto = new CallBackMetaEventDto();
        callBackMetaEventDto.setVendorName("NonAvaya");
        callBackMetaEventDto.setDirection(CallDirection.INBOUND);

        callStartEventsJoiner.updateCallDirection(callEventDto, callBackMetaEventDto);

        assertEquals(CallDirection.INBOUND, callEventDto.getCallDirection());
    }
}