package com.observeai.platform.realtime.neutrino.client.http;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class CustomProxySelector extends ProxySelector {
    private final String proxyHost;
    private final int proxyPort;
    private final List<String> listOfDomainsToSkipProxy;

    CustomProxySelector(String proxyHost, int proxyPort, List<String> doNotUseProxyDomains) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.listOfDomainsToSkipProxy = doNotUseProxyDomains;
    }

    private boolean hostMatchesTheSkipProxyDomains(String host, List<String> domains) {
        if (domains != null || !(domains.size()>0)) {
            for (String s : domains) {
                if (host.contains(s))
                    return true;
            }
        }
        return false;
    }

    @Override
    public List<Proxy> select(URI uri) {
        String host = uri.getHost();
        if (hostMatchesTheSkipProxyDomains(host, listOfDomainsToSkipProxy)) {
            List<Proxy> proxyList = new ArrayList<>();
            proxyList.add(Proxy.NO_PROXY);
            return proxyList;
        }
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        List<Proxy> proxyList = new ArrayList<>();
        proxyList.add(proxy);
        return proxyList;

    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        //Todo: log error
    }
}
