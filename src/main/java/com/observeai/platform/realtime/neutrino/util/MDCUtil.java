package com.observeai.platform.realtime.neutrino.util;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class MDCUtil {

    /**
     * Wraps a Runnable with MDC context, ensuring MDC values are properly propagated to the new thread.
     * The method preserves the previous MDC context and restores it after the wrapped Runnable completes.
     *
     * @param runnable    The Runnable to be wrapped with MDC context
     * @param contextMap  Map containing MDC key-value pairs to be set in the new thread
     * @return           A new Runnable that handles MDC context propagation
     */
    public static Runnable wrapWithMDC(Runnable runnable, HashMap<String, String> contextMap) {
        return () -> {
            if (contextMap == null)
                return;
            contextMap.values().removeIf(Objects::isNull);
            Map<String, String> previousContext = MDC.getCopyOfContextMap();
            try {
	            MDC.setContextMap(contextMap);
	            runnable.run();
            } finally {
                if (previousContext == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previousContext);
                }
            }
        };
    }
}