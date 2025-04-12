package com.observeai.platform.realtime.neutrino.context;

public class ContextThreadLocal {
    private final static ThreadLocal<ObserveContext> observeContextThreadLocal = new ThreadLocal<>();

    public static ObserveContext getObserveContext() {
        ObserveContext context = observeContextThreadLocal.get();
        if (context == null) {
            context = new ObserveContext();
            observeContextThreadLocal.set(context);
        }
        return context;
    }

    public static void setObserveContext(ObserveContext context) {
        observeContextThreadLocal.set(context);
    }

    public static void removeObserveContext() {
        observeContextThreadLocal.remove();
    }
}

