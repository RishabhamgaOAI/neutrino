package com.observeai.platform.realtime.neutrino.service.five9;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.observeai.platform.realtime.neutrino.NeutrinoBaseTest;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionResponse;
import com.observeai.platform.realtime.neutrino.exception.InternalAppErrorResponse;
import com.observeai.platform.realtime.neutrino.exception.dravity.DravityExceptions;
import com.observeai.platform.realtime.neutrino.exception.neutrino.NeutrinoExceptions;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProducer;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.kafka.KafkaTopics;
import com.observeai.platform.realtime.neutrino.redis.CallMetadataRedisStore;
import com.observeai.platform.realtime.neutrino.service.impl.five9.Five9ServiceImpl;
import com.observeai.platform.realtime.neutrino.util.five9.Five9Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.observeai.platform.realtime.neutrino.utils.Five9TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
class Five9ServiceTest extends NeutrinoBaseTest {

    @Mock
    DirectiveService directiveService;
    @Mock
    SubscriptionService subscriptionService;
    @Mock
    KafkaProducer kafkaProducer;
    @Mock
    KafkaProperties kafkaProperties;
    @Mock
    CallMetadataRedisStore callMetadataRedisStore;
    @Spy @InjectMocks
    Five9Util five9Util;

    Five9Service five9Service;

    @BeforeEach
     void init() {
        five9Service = new Five9ServiceImpl(kafkaProducer, kafkaProperties, subscriptionService, directiveService, five9Util, callMetadataRedisStore);
    }

    @Test
    void createSubscription_NoDirectiveExist_ShouldCreateDirectiveAndAttach() {

        when(subscriptionService.onCreate(sampleCreateSubscriptionNotification())).thenReturn(sampleSubscriptionResponse());
        when(directiveService.getDirectiveId(SAMPLE_DOMAIN_ID)).thenReturn(Optional.ofNullable(null));
        when(directiveService.createDirective(SAMPLE_DOMAIN_ID)).thenReturn(sampleDirectiveResponse());
        doNothing().when(directiveService).attachDirectiveToSubscription(SAMPLE_DOMAIN_ID, SAMPLE_DIRECTIVE_ID, SAMPLE_SUBSCRIPTION_ID);
        doNothing().when(five9Util).persistDirectiveId(SAMPLE_DIRECTIVE_ID, SAMPLE_DOMAIN_ID);

        SubscriptionResponse response = five9Service.handleSubscriptionNotification(sampleCreateSubscriptionNotification());
        verify(directiveService, times(1)).createDirective(SAMPLE_DOMAIN_ID);
        assertThat(response.getDirectiveId()).isEqualTo(SAMPLE_DIRECTIVE_ID);
    }

    @Test
    void createSubscription_DirectiveAlreadyExists_ShouldAttachDirectly() {

        when(subscriptionService.onCreate(sampleCreateSubscriptionNotification())).thenReturn(sampleSubscriptionResponse());
        when(directiveService.getDirectiveId(SAMPLE_DOMAIN_ID)).thenReturn(Optional.of(SAMPLE_DIRECTIVE_ID));
        doNothing().when(directiveService).attachDirectiveToSubscription(SAMPLE_DOMAIN_ID, SAMPLE_DIRECTIVE_ID, SAMPLE_SUBSCRIPTION_ID);

        SubscriptionResponse response = five9Service.handleSubscriptionNotification(sampleCreateSubscriptionNotification());
        verify(directiveService, times(0)).createDirective(SAMPLE_DOMAIN_ID);
        assertThat(response.getDirectiveId()).isEqualTo(SAMPLE_DIRECTIVE_ID);
    }

    @Test
    void handleCallConnectedEvent_shouldPushCallStartEvent() throws JsonProcessingException {
        doReturn(sampleAccountAndUserInfo()).when(five9Util).getAccountAndUserInfo(any(), any());
        when(kafkaProperties.getTopics()).thenReturn(new KafkaTopics());
        doNothing().when(kafkaProducer).produceMessageInCamelCase(any(), any());
        doNothing().when(kafkaProducer).produceMessage(any(), any(String.class), any(CallBackMetaEventDto.class));

        five9Service.handleCallEvents(sampleCallConnectedEvent());
        verify(kafkaProducer, times(1)).produceMessage(any(), any(String.class), any(CallBackMetaEventDto.class));
        verify(kafkaProducer, times(0)).produceMessageInCamelCase(any(), any());
    }

    @Test
    void handleEventWhenAccountDoesntExist_shouldThrowException() {
        DravityExceptions.ResourceNotFoundException ex = new DravityExceptions.ResourceNotFoundException(new InternalAppErrorResponse("404", "error_desc"));
        doThrow(ex).when(five9Util).getAccountAndUserInfo(any(), any());

        assertThatThrownBy(() -> five9Service.handleCallEvents(sampleCallConnectedEvent())).isInstanceOf(NeutrinoExceptions.UserAccountException.class);
    }

    @Test
    void handleEventWhenBadRequestException_shouldThrowException() {
        DravityExceptions.BadRequestException ex = new DravityExceptions.BadRequestException(new InternalAppErrorResponse("404", "error_desc"));
        doThrow(ex).when(five9Util).getAccountAndUserInfo(any(), any());

        assertThatThrownBy(() -> five9Service.handleCallEvents(sampleCallConnectedEvent())).isInstanceOf(NeutrinoExceptions.UserAccountException.class);
    }

}
