package com.observeai.platform.realtime.neutrino.client.http;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeyStoreProperties {
    private String trustStore;
    private String keyStore;
    private String trustAndKeyStoreAndKeyPassword;
    private boolean trustAllCerts;
}
