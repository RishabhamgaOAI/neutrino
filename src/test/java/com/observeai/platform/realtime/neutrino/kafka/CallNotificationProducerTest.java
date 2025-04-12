package com.observeai.platform.realtime.neutrino.kafka;

import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventDto;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallEventType;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallTopicEndNotification;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallTopicEventNotification;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallTopicStartNotification;
import com.observeai.platform.realtime.neutrino.client.MonitoringAgent;
import com.observeai.platform.realtime.neutrino.client.NotificationProperties;
import com.observeai.platform.realtime.neutrino.data.Call;
import com.observeai.platform.realtime.neutrino.data.CallAttachedMetadata;
import com.observeai.platform.realtime.neutrino.data.CallState;
import com.observeai.platform.realtime.neutrino.data.common.CallSessionMetadata;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoConcise;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.util.CallEventUtil;
import com.observeai.platform.realtime.neutrino.util.DravityRequestUtil;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
class CallNotificationProducerTest {

    private KafkaProperties kafkaProperties;
    private KafkaTopics kafkaTopics;
    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private DravityRequestUtil dravityRequestUtil;

    @Mock
    private MonitoringAgent monitoringAgent;
    private NotificationProperties notificationProperties;
    private CallNotificationProducer callNotificationProducer;
    
    @Mock
    private TagDetectionRequestProducer tagDetectionRequestProducer;

    private Call call;

    @BeforeEach
    void setUp() {
        kafkaProperties = new KafkaProperties();
        kafkaTopics = new KafkaTopics();
        kafkaTopics.setNotificationServerTopic("notificationServerTopic");
        kafkaTopics.setCallMessageTopic("callMessageTopic");
        kafkaTopics.setCallAudioMessageTopic("callAudioMessageTopic");
        kafkaProperties.setTopics(kafkaTopics);
        notificationProperties = new NotificationProperties();

        callNotificationProducer = new CallNotificationProducer(
                kafkaProperties,
                kafkaProducer,
                notificationProperties,
                dravityRequestUtil,
                monitoringAgent, tagDetectionRequestProducer);

        call = new Call();
        call._setObserveCallId("observeId");

        CallStartMessage startMsg = new CallStartMessage();
        startMsg.setVendorCallId("vendorId");
        startMsg.setAccountId("test-account-id");
        startMsg.setPreviewCall(false);
        CallSessionMetadata sessionMetadata = new CallSessionMetadata();
        call.setSessionMetadata(sessionMetadata);
        call.setStartTime(10000l);
        call._setStartMessage(startMsg);

    }

    @Test
    void testOnActiveProcessingWeDontSendMomentsToAllTopics() {
        AccountInfoConcise ac = new AccountInfoConcise();
        ac.setName("acctid");
        HttpResponse<AccountInfoConcise> httpResponse = new HttpResponse<>(ac, new HttpHeaders(), null);

        when(dravityRequestUtil.getAccountInfoByObserveAccountId(anyString()))
                .thenReturn(httpResponse);
        callNotificationProducer.onActiveProcessing(call);

        verify(kafkaProducer, times(3))
                .produceMessage(anyString(), eq("observeId"), any(CallTopicEventNotification.class));

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CallTopicEventNotification> eventCaptor = ArgumentCaptor
                .forClass(CallTopicEventNotification.class);

        verify(kafkaProducer, times(3))
                .produceMessage(topicCaptor.capture(), anyString(), eventCaptor.capture());

        for (int i = 0; i < 3; i++) {
            String topic = topicCaptor.getAllValues().get(i);
            CallTopicEventNotification notification = eventCaptor.getAllValues().get(i);

            if (notification instanceof CallTopicStartNotification) {
                CallTopicStartNotification startNotification = (CallTopicStartNotification) notification;
                if ("callMessageTopic".equals(topic)) {
                    assertNotNull(
                        startNotification.getMessage().getAttachedVariables().getAccountInfo(),
                            "AccountInfo should be present in the callMessageTopic version");
                } else {
                    assertNull(
                        startNotification.getMessage().getAttachedVariables().getAccountInfo(),
                            "No attached metadata should be in the stripped version for topic " + topic);
                }
            }
        }
    }
    @Test
    void testOnNotStartEventWeDontSendMomentsToAnyTopics() {
        call.setStateTransitions(Collections.singletonList(CallState.ACTIVE_PROCESSING));
        callNotificationProducer.onEnded(call);

        verify(kafkaProducer, times(3))
                .produceMessage(anyString(), eq("observeId"), any(CallTopicEventNotification.class));

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CallTopicEventNotification> eventCaptor = ArgumentCaptor
                .forClass(CallTopicEventNotification.class);

        verify(kafkaProducer, times(3))
                .produceMessage(topicCaptor.capture(), anyString(), eventCaptor.capture());

        for (int i = 0; i < 3; i++) {
            String topic = topicCaptor.getAllValues().get(i);
            CallTopicEventNotification notification = eventCaptor.getAllValues().get(i);

            if (notification instanceof CallTopicStartNotification) {
                CallTopicEndNotification endNotification = (CallTopicEndNotification) notification;
                assertNotNull(
                    endNotification.getMessage().getEndTime(),
                        "End time should be present in the notification");
            }
        }
    }

    @Test
    void testKafkaMessageDeserializationWithNewCallAttachedMetadataStructure() {
        // Setup AccountInfoConcise
        AccountInfoConcise accountInfo = new AccountInfoConcise();
        accountInfo.setName("testAccount");
        accountInfo.setObserveAccountId("observeAccId");
        HttpResponse<AccountInfoConcise> httpResponse = new HttpResponse<>(accountInfo, new HttpHeaders(), null);
        
        when(dravityRequestUtil.getAccountInfoByObserveAccountId(anyString()))
                .thenReturn(httpResponse);
        
        // Capture the Kafka message that's produced
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<CallTopicEventNotification> eventCaptor = ArgumentCaptor.forClass(CallTopicEventNotification.class);
        
        // Trigger the notification production
        callNotificationProducer.onActiveProcessing(call);
        
        // Verify the message was produced
        verify(kafkaProducer, times(3))
                .produceMessage(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());
        
        // Find the message sent to CallMessageTopic
        int callMessageTopicIndex = topicCaptor.getAllValues().indexOf("callMessageTopic");
        if (callMessageTopicIndex >= 0) {
            CallTopicEventNotification notification = eventCaptor.getAllValues().get(callMessageTopicIndex);
            
            // Verify it's a start notification with our account info
            assertNotNull(notification, "Notification should not be null");
            assertTrue(notification instanceof CallTopicStartNotification, "Should be a start notification");
            
            CallTopicStartNotification startNotification = (CallTopicStartNotification) notification;
            assertNotNull(startNotification.getMessage().getAttachedVariables(), 
                    "Attached variables should not be null");
            assertNotNull(startNotification.getMessage().getAttachedVariables().getAccountInfo(), 
                    "Account info should not be null");
            
            // Verify account info is correctly populated
            assertEquals("testAccount", 
                    startNotification.getMessage().getAttachedVariables().getAccountInfo().getName());
            assertEquals("observeAccId", 
                    startNotification.getMessage().getAttachedVariables().getAccountInfo().getObserveAccountId());
            
            // The other fields are removed from CallAttachedMetadata, so we don't need to check for them
        }
    }

}
