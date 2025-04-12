package com.observeai.platform.realtime.neutrino.config;

import com.observeai.platform.realtime.commons.data.messages.CallStreamerEvent;
import com.observeai.platform.realtime.commons.data.messages.notifications.CallTopicStartNotification;
import com.observeai.platform.realtime.neutrino.data.dto.CallBackMetaEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.CallStartMessage;
import com.observeai.platform.realtime.neutrino.kafka.KafkaProperties;
import com.observeai.platform.realtime.neutrino.properties.ThreadPoolProperties;
import com.observeai.platform.realtime.neutrino.service.CallStartMessageFilterStrategy;
import com.observeai.platform.realtime.threadpoolutils.factory.MonitoredThreadPoolFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Configuration
public class KafkaConsumerConfig {
	private final KafkaProperties kafkaProperties;
	private final ConsumerAwareRebalanceListener customRebalanceListener;
	@Value("${spring.profiles.active:Unknown}")
	private String activeProfile;
	private final ThreadPoolProperties threadPoolProperties;
	private final MonitoredThreadPoolFactory monitoredThreadPoolFactory;
	private final CallStartMessageFilterStrategy callStartMessageFilterStrategy;

	@Bean
	public ThreadPoolTaskExecutor kafkaProcessingThreadPool() {
		return monitoredThreadPoolFactory.createThreadPoolTaskExecutor(threadPoolProperties.getKafkaProcessingThreadPool().getCorePoolSize(),
				threadPoolProperties.getKafkaProcessingThreadPool().getMaxPoolSize(), Integer.MAX_VALUE, "kafka-processor-");
	}

	public <T> ConsumerFactory<String, T> abstractConsumerFactory(Class<T> valueClass) {
		return new DefaultKafkaConsumerFactory<>(getCommonConsumerProps(), new StringDeserializer(),
				new ErrorHandlingDeserializer<>(new JsonDeserializer<>(valueClass)));
	}

	public <T> ConcurrentKafkaListenerContainerFactory<String, T> concurrentKafkaListenerContainerFactory(Class<T> valueClass) {
		ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(abstractConsumerFactory(valueClass));
		factory.getContainerProperties().setConsumerRebalanceListener(customRebalanceListener);
		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CallTopicStartNotification> callNotificationListenerContainerFactory() {
		return concurrentKafkaListenerContainerFactory(CallTopicStartNotification.class);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CallBackMetaEventDto> callBackMetaEventListenerContainerFactory() {
		return concurrentKafkaListenerContainerFactory(CallBackMetaEventDto.class);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CallStartMessage> callStartMessageListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, CallStartMessage> factory = concurrentKafkaListenerContainerFactory(CallStartMessage.class);
		factory.setRecordFilterStrategy(callStartMessageFilterStrategy);
		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CallStreamerEvent> callStreamerEventListenerContainerFactory() {
		return concurrentKafkaListenerContainerFactory(CallStreamerEvent.class);
	}


	public Map<String, Object> getCommonConsumerProps() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, "neutrino-consumer" + UUID.randomUUID());
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.getEnableAutoCommit());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		if (!activeProfile.equals("test")) {
			props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
			props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
			props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""+kafkaProperties.getKey()+"\" password=\""+kafkaProperties.getSecret()+"\";");
			props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 15_000);
			props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30_000);
			props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 30_000);
		}
		return props;
	}
}
