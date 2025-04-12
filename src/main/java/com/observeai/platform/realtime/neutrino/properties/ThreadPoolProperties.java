package com.observeai.platform.realtime.neutrino.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "thread-pools")
public class ThreadPoolProperties {
	private ConfigurableThreadPool tomcat;
	private ConfigurableThreadPool kafkaMetricsThreadPool;
	private FixedThreadPool callReconnectionPool;
	private ConfigurableThreadPool kafkaProcessingThreadPool;
	private FixedThreadPool callEventMetricsPool;


	@Getter
	@Setter
	public static class ConfigurableThreadPool {
		private int corePoolSize;
		private int maxPoolSize;
	}

	@Getter
	@Setter
	public static class FixedThreadPool {
		private int fixedPoolSize;
	}
}
