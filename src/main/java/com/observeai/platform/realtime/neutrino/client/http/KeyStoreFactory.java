/*
package com.observeai.platform.realtime.neutrino.client.http;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.PrivateKeyDetails;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

public class KeyStoreFactory {

    private final SSLConnectionSocketFactory socketFactory;

    public KeyStoreFactory(KeyStoreProperties keyStoreProperties) {
        try {
            if (keyStoreProperties.isTrustAllCerts()) {
                SSLContext sslcontext = SSLContexts.custom()
                        .loadTrustMaterial(getFile(keyStoreProperties.getTrustStore()), keyStoreProperties.getTrustAndKeyStoreAndKeyPassword().toCharArray(), (TrustStrategy) (x509Certificates, s) -> true)
                        .loadKeyMaterial(getFile(keyStoreProperties.getKeyStore()), keyStoreProperties.getTrustAndKeyStoreAndKeyPassword().toCharArray(), keyStoreProperties.getTrustAndKeyStoreAndKeyPassword().toCharArray())
                        .build();
                socketFactory = new SSLConnectionSocketFactory(sslcontext);
            } else {
                SSLContext sslcontext = SSLContexts.custom()
                        .loadTrustMaterial(getFile(keyStoreProperties.getTrustStore()), keyStoreProperties.getTrustAndKeyStoreAndKeyPassword().toCharArray())
                        .loadKeyMaterial(getFile(keyStoreProperties.getKeyStore()), keyStoreProperties.getTrustAndKeyStoreAndKeyPassword().toCharArray(), keyStoreProperties.getTrustAndKeyStoreAndKeyPassword().toCharArray())
                        .build();

                socketFactory = new SSLConnectionSocketFactory(sslcontext);
            }


        } catch (Exception e) {
            throw new IllegalStateException(String.format("Unable to load key/trust store Http Connection with trustStore %s, keyStore %s ", keyStoreProperties.getTrustStore(), keyStoreProperties.getKeyStore()));
        }
    }

    SSLConnectionSocketFactory getSocketFactory() {
        return socketFactory;
    }

    private File getFile(String fileName) {
        File file = null;
        try {
            final URL resourceAsStream = this.getClass().getResource(fileName);
            file = new File(resourceAsStream.getFile());
        } catch (Exception e) {
            file =  new File(fileName);
        }
        return file;
    }

    private class PrivateKeyLoadStrategy implements PrivateKeyStrategy {
        @Override
        public String chooseAlias(Map<String, PrivateKeyDetails> map, Socket socket) {
            return null;
        }
    }

}
*/
