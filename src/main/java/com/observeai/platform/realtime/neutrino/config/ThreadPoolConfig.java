package com.observeai.platform.realtime.neutrino.config;

import com.observeai.platform.realtime.neutrino.properties.ThreadPoolProperties;
import com.observeai.platform.realtime.threadpoolutils.factory.MonitoredThreadPoolFactory;
import com.observeai.platform.realtime.threadpoolutils.factory.impl.MonitoredThreadPoolFactoryImpl;
import com.observeai.platform.realtime.threadpoolutils.metrics.ThreadPoolMetricCollector;
import com.observeai.platform.realtime.threadpoolutils.metrics.latency.LatencyProfilerStore;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ScheduledExecutorService;

@Configuration
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class ThreadPoolConfig {
	private final ThreadPoolProperties threadPoolProperties;

	@Bean
	public LatencyProfilerStore threadPoolLatencyProfilerStore() {
		return new LatencyProfilerStore();
	}

	@Bean
	public MonitoredThreadPoolFactory monitoredThreadPoolFactory(LatencyProfilerStore threadPoolLatencyProfilerStore) {
		return new MonitoredThreadPoolFactoryImpl(threadPoolLatencyProfilerStore);
	}

	@Bean
	public ThreadPoolMetricCollector threadPoolMetricCollector(LatencyProfilerStore threadPoolLatencyProfilerStore, MonitoredThreadPoolFactory monitoredThreadPoolFactory) {
		return new ThreadPoolMetricCollector(threadPoolLatencyProfilerStore, monitoredThreadPoolFactory);
	}

	@Bean("kafkaMetricsTaskExecutor")
	public ThreadPoolTaskExecutor kafkaMetricsTaskExecutor(MonitoredThreadPoolFactory monitoredThreadPoolFactory) {
		return monitoredThreadPoolFactory.createThreadPoolTaskExecutor(threadPoolProperties.getKafkaMetricsThreadPool().getCorePoolSize(),
				threadPoolProperties.getKafkaMetricsThreadPool().getMaxPoolSize(), Integer.MAX_VALUE, "kafka-metrics-");
	}

	@Bean("reconnectionTaskExecutor")
	public ScheduledExecutorService reconnectionTaskExecutor(MonitoredThreadPoolFactory monitoredThreadPoolFactory) {
		return monitoredThreadPoolFactory.createScheduledThreadPoolExecutor(
				threadPoolProperties.getCallReconnectionPool().getFixedPoolSize(), "reconnection-");
	}

	@Bean("callEventMetricsTaskExecutor")
	public ScheduledExecutorService callEventMetricsTaskExecutor(MonitoredThreadPoolFactory monitoredThreadPoolFactory) {
		return monitoredThreadPoolFactory.createScheduledThreadPoolExecutor(
				threadPoolProperties.getCallEventMetricsPool().getFixedPoolSize(), "call-event-metrics-");
	}

	@Bean("neutrinoTomcatPool")
	public ThreadPoolTaskExecutor customTomcatThreadPool(MonitoredThreadPoolFactory monitoredThreadPoolFactory) {
		return monitoredThreadPoolFactory.createThreadPoolTaskExecutor(threadPoolProperties.getTomcat().getCorePoolSize(),
				threadPoolProperties.getTomcat().getMaxPoolSize(), Integer.MAX_VALUE, "tomcat-");
	}

	@Bean
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer(MonitoredThreadPoolFactory monitoredThreadPoolFactory) {
		return tomcatServletWebServerFactory -> tomcatServletWebServerFactory.addConnectorCustomizers(connector -> {
			Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
			protocol.setExecutor(customTomcatThreadPool(monitoredThreadPoolFactory));
		});
	}

}
