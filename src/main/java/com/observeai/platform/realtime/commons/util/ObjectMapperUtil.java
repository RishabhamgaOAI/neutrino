package com.observeai.platform.realtime.commons.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.commons.data.messages.details.DeepgramMessage;
import com.observeai.platform.realtime.neutrino.util.ObjectMapperFactory;

import java.io.IOException;

public class ObjectMapperUtil {

    private static final ObjectMapper objectMapper = ObjectMapperFactory.getSnakeCaseObjectMapper();

    public static DeepgramMessage deserializeDeepgramResponse(String response)
            throws IOException {
        return objectMapper.readValue(response, DeepgramMessage.class);
    }
}
