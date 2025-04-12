package com.observeai.platform.realtime.neutrino.service;

import com.observeai.platform.realtime.neutrino.config.CallSourceConfig;
import com.observeai.platform.realtime.neutrino.config.CallSourceConfigs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CallSourceConfigService {
    private final static String DEFAULT_KEY = "default";
    private final CallSourceConfigs callSourceConfigs;

    public Optional<CallSourceConfig> getCallSourceConfig(String name, String key) {
        if (callSourceConfigs.getEntries() == null) {
            log.error("Call source config is not configured");
            return Optional.empty();
        }

        if (!callSourceConfigs.getEntries().containsKey(name)) {
            log.error("Name: {} missing in call source config", name);
            return Optional.empty();
        }

        for (Map.Entry<String, CallSourceConfig> configs : callSourceConfigs.getEntries().get(name).entrySet()) {
            if (configs.getKey().equalsIgnoreCase(key)) {
                return Optional.of(configs.getValue());
            }
        }
        log.error("Key: {} missing in call source config for name: {}", key, name);
        return DEFAULT_KEY.equalsIgnoreCase(key) ? Optional.empty() : getCallSourceConfig(name, DEFAULT_KEY);
    }

    public Optional<CallSourceConfig> getCallSourceConfig(String name, CallSourceConfig mediaFormat) {
        if (mediaFormat == null || getKey(mediaFormat) == null) {
            log.info("No/partial media format found, returning default call source config");
            return getCallSourceConfig(name, DEFAULT_KEY);
        }
        else
            return getCallSourceConfig(name, getKey(mediaFormat));
    }

    private String getKey(CallSourceConfig callSourceConfig) {
        if (callSourceConfig.getEncoding() == null) {
            log.info("No encoding found in media format");
            return null;
        }
        return callSourceConfig.getEncoding() + "-" + callSourceConfig.getSampleRate();
    }

    public static CallSourceConfig parse(JSONObject media) {
        if (media == null) {
            log.warn("No media message present. Returning null");
            return null;
        }
        CallSourceConfig config = new CallSourceConfig();

        Class<?> configClass = CallSourceConfig.class;
        Field[] fields = configClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (Modifier.isStatic(field.getModifiers()))
                    continue;
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = media.opt(fieldName);
                if (value != null)
                    field.set(config, value);
            } catch (Throwable th) {
                log.error("Unable to parse media message, fallback to default values. mediaMessage: {}", media, th);
                return null;
            }
        }
        return config;
    }
}
