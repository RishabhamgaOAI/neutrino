package com.observeai.platform.realtime.neutrino.util;

import com.observeai.platform.realtime.neutrino.NeutrinoBaseTest;
import com.observeai.platform.realtime.neutrino.data.common.CallDirection;
import com.observeai.platform.realtime.neutrino.data.dto.five9.CallEvent;
import com.observeai.platform.realtime.neutrino.data.dto.five9.CallEvent.CallDetails;
import com.observeai.platform.realtime.neutrino.util.CallDirectionResolver.Five9CallDirectionResolver;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CallDirectionResolverTest extends NeutrinoBaseTest {

    @Test
    public void validateFive9InboundCallType() {
        Five9CallDirectionResolver five9CallDirectionResolver = new Five9CallDirectionResolver();
        assertEquals(five9CallDirectionResolver.getCallDirection(getFive9CallEventWithType("Inbound")), CallDirection.INBOUND);
    }

    @Test
    public void validateFive9OutboundCallType() {
        Five9CallDirectionResolver five9CallDirectionResolver = new Five9CallDirectionResolver();
        assertEquals(five9CallDirectionResolver.getCallDirection(getFive9CallEventWithType("Outbound")), CallDirection.OUTBOUND);
        assertEquals(five9CallDirectionResolver.getCallDirection(getFive9CallEventWithType("Manual")), CallDirection.OUTBOUND);
        assertEquals(five9CallDirectionResolver.getCallDirection(getFive9CallEventWithType("Autodial")), CallDirection.OUTBOUND);
    }

    private CallEvent getFive9CallEventWithType(String typeName) {
        return new CallEvent(null, new CallDetails(null, typeName, null, null, null, null));
    }
}
