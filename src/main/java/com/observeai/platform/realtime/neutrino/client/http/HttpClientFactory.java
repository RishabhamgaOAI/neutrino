package com.observeai.platform.realtime.neutrino.client.http;


import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;

public class HttpClientFactory {
    private final CloseableHttpClient threadSafeHttpClient;

    public HttpClientFactory(HttpProperties httpProperties) {
        //List<String> domainsToSkipProxy = getList(httpProperties.getDomainsToSkipViaProxy());

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                //.register("https", keyStoreFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();


        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager proxiedConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // Create socket configuration
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .setSoTimeout(Timeout.ofMilliseconds(httpProperties.getSocketTimeOutInMillis()))
                .build();
        // Configure the connection manager to use socket configuration either
        // by default or for a specific host.
        proxiedConnManager.setDefaultSocketConfig(socketConfig);

        // Validate connections after 1 sec of inactivity
        proxiedConnManager.setValidateAfterInactivity(Timeout.ofMilliseconds(httpProperties.getInactivityValidationIntervalInMs()));

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        proxiedConnManager.setMaxTotal(httpProperties.getMaxTotalConnections());
        proxiedConnManager.setDefaultMaxPerRoute(httpProperties.getMaxConnectionsPerRoute());

        /*connManager.setDefaultSocketConfig(socketConfig);
        connManager.setValidateAfterInactivity(httpProperties.getInactivityValidationIntervalInMs());
        connManager.setMaxTotal(httpProperties.getMaxTotalConnections());
        connManager.setDefaultMaxPerRoute(httpProperties.getMaxConnectionsPerRoute());*/

        //SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(new CustomProxySelector(httpProperties.getProxyHttpHost(), httpProperties.getProxyHttpPort(), domainsToSkipProxy));


        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCircularRedirectsAllowed(true)
                .setExpectContinueEnabled(httpProperties.isExpectContinueEnabled())
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(httpProperties.getConnectionRequestTimeout()))
                .build();

        threadSafeHttpClient = HttpClients.custom()
                .setConnectionManager(proxiedConnManager)
                .setDefaultRequestConfig(defaultRequestConfig)
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.ofMilliseconds(1000)))
                .evictExpiredConnections()
                .evictIdleConnections(Timeout.ofMilliseconds(httpProperties.getIdleConnectionEvictionTimeout()))
                .build();

    }

    public CloseableHttpClient getHttpClient() {
        return threadSafeHttpClient;
    }

    public void shutDown() {
        try {
            if (threadSafeHttpClient != null)
                threadSafeHttpClient.close();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to close the Httpclient  due to IOException, e");
        }
    }
}

