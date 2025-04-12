package com.observeai.platform.realtime.neutrino.kafka;

import com.observeai.platform.realtime.commons.data.messages.TagDetectionRequest;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.util.Constants;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class TagDetectionRequestProducerTest {

    @Mock
    private KafkaProperties kafkaProperties;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private TagDetectionRequestProducer tagDetectionRequestProducer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testOnActiveProcessing_whenMetadataBasedScriptsEnabled() {
        // Mock the Call object
        Call call = mock(Call.class);
        CallStartMessage startMessage = mock(CallStartMessage.class);
        KafkaTopics kafkaTopics = mock(KafkaTopics.class);

        // Set up the mock behavior
        when(call.isMetadataBasedScriptsEnabled()).thenReturn(true);
        when(call.getVendor()).thenReturn(Constants.NICE);
        when(call.getStartMessage()).thenReturn(startMessage);

        // Mock KafkaProperties and KafkaTopics
        when(kafkaProperties.getTopics()).thenReturn(kafkaTopics);
        when(kafkaTopics.getCallMessageTopic()).thenReturn("mocked-topic");

        // Mock the observe call ID
        when(call.getObserveCallId()).thenReturn("mocked-observe-call-id");

        // Call the method under test
        tagDetectionRequestProducer.publishTagDetectionRequest(call);

        // Verify the interactions
        verify(kafkaProducer, times(1)).produceMessage(eq("mocked-topic"), eq("mocked-observe-call-id"), any(TagDetectionRequest.class));
    }
}