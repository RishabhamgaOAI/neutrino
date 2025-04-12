package com.observeai.platform.realtime.neutrino.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.util.ObjectMapperFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SlackClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = ObjectMapperFactory.getPascalCaseObjectMapper();
    @Value("${slack.webhook}")
    private String slackChannelUrl;

    @Async
    public void sendMessage(String message) {
        Map<String, String> messageBody = new HashMap<>();
        messageBody.put("text", message);
        try {
            String messageJson = objectMapper.writeValueAsString(messageBody);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(messageJson, headers);
            restTemplate.exchange(slackChannelUrl, HttpMethod.POST, entity, String.class);
        } catch (IOException e) {
            log.error("failed to serialize slack message");
        }
    }
}
