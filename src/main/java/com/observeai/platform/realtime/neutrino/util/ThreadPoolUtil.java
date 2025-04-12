package com.observeai.platform.realtime.neutrino.util;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ThreadPoolUtil {

	public static ExecutorService createFixedThreadPool(int corePoolSize, String threadNamePrefix) {
		return Executors.newFixedThreadPool(corePoolSize, new CustomizableThreadFactory(threadNamePrefix));
	}

	public static ScheduledExecutorService createScheduledThreadPool(int corePoolSize, String threadNamePrefix) {
		return Executors.newScheduledThreadPool(corePoolSize, new CustomizableThreadFactory(threadNamePrefix));
	}
}
