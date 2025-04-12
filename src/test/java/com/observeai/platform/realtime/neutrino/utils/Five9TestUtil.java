package com.observeai.platform.realtime.neutrino.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.data.dto.AccountAndUserInfoResponseDto;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto;
import com.observeai.platform.realtime.neutrino.data.dto.UserMapping;
import com.observeai.platform.realtime.neutrino.data.dto.five9.directive.DirectiveRequest;
import com.observeai.platform.realtime.neutrino.data.dto.five9.directive.DirectiveResponse;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionNotification;
import com.observeai.platform.realtime.neutrino.data.dto.five9.subscription.SubscriptionResponse;
import com.observeai.platform.realtime.neutrino.exception.five9.Five9ErrorResponse;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

public class Five9TestUtil {

    public static String SAMPLE_SUBSCRIPTION_ID = "sub1";
    public static String SAMPLE_DOMAIN_ID = "130376";
    public static String SAMPLE_DIRECTIVE_ID = "directive1";
    public static String SAMPLE_CLIENT_ID = "client1";
    public static String SAMPLE_ACCESS_TOKEN = null;
    public static String SAMPLE_TRUST_TOKEN = "trustToken1";

    public static AccountAndUserInfoResponseDto sampleAccountAndUserInfo() {
        return new AccountAndUserInfoResponseDto(new AccountInfoWithVendorDetailsDto(),
                new UserMapping());
    }
    public static SubscriptionResponse sampleSubscriptionResponse() {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setSubscriptionId(SAMPLE_SUBSCRIPTION_ID);
        response.setDirectiveId(SAMPLE_DIRECTIVE_ID);
        response.setStatus("active");
        return response;
    }

    public static SubscriptionNotification sampleCreateSubscriptionNotification() {
        SubscriptionNotification notification = new SubscriptionNotification();
        notification.setSubscriptionId(SAMPLE_SUBSCRIPTION_ID);
        notification.setNotificationType("create");
        notification.setDomainId(SAMPLE_DOMAIN_ID);
        return notification;
    }

    public static DirectiveResponse sampleDirectiveResponse() {
        DirectiveResponse directiveResponse = new DirectiveResponse();
        directiveResponse.setDirectiveId(SAMPLE_DIRECTIVE_ID);
        return directiveResponse;
    }

    public static DirectiveRequest sampleDirectiveRequest() {
        DirectiveRequest directiveRequest = new DirectiveRequest();
        directiveRequest.setTrustToken(SAMPLE_TRUST_TOKEN);
        return directiveRequest;
    }

    public static byte[] sampleFive9ErrorBody() {
        Five9ErrorResponse.HTTPErrorDetail httpErrorDetail = new Five9ErrorResponse.HTTPErrorDetail("code", "message", "path", "invalidValue");
        List<Five9ErrorResponse.HTTPErrorDetail> httpErrorDetailList = new ArrayList<>();
        httpErrorDetailList.add(httpErrorDetail);
        Five9ErrorResponse five9ErrorResponse = new Five9ErrorResponse("trace", httpErrorDetailList);
        try {
            return new ObjectMapper().writeValueAsBytes(five9ErrorResponse);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static RestClientResponseException sample5xxResponse() {
        return new RestClientResponseException(null, 500, null, null, sampleFive9ErrorBody(), null);
    }

    public static RestClientResponseException sample4xxResponse() {
        return new RestClientResponseException(null, 400, null, null, sampleFive9ErrorBody(), null);
    }

    public static HttpHeaders sampleHttpHeaderWithToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(SAMPLE_ACCESS_TOKEN);
        return headers;
    }

    public static <T> HttpResponse<T> buildHttpResponse(T response, HttpStatus code) {
        return new HttpResponse<T>(response, null, null);
    }

    public static JsonNode sampleCallConnectedEvent() {
        String event = "{\n" +
                "    \"header\": {\n" +
                "        \"subscriptionId\": \"1c64c341f39a48f49437410232cf7bb8\",\n" +
                "        \"orgId\": \"130736\",\n" +
                "        \"domainId\": \"130736\",\n" +
                "        \"eventId\": \"507F52DFCA684C9FAD7B109711E1FC09\",\n" +
                "        \"eventTimestamp\": \"2022-01-19T16:34:18.517\",\n" +
                "        \"eventGroup\": \"InteractionEvent\",\n" +
                "        \"eventName\": \"Accepted\"\n" +
                "    },\n" +
                "    \"payload\": {\n" +
                "        \"CallEnded\": {\n" +
                "            \"party\": {\n" +
                "                \"Call\": {\n" +
                "                    \"ani\": \"7122215936\",\n" +
                "                    \"dnis\": \"7122214593\",\n" +
                "                    \"list_id\": 0\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"attached_variables\": {\n" +
                "            \"Call.session_id\": \"F6A99879178C4E099B0C4172CB0E6743\",\n" +
                "            \"Call.type_name\": \"Inbound\",\n" +
                "            \"Call.call_id\": \"962\",\n" +
                "            \"IVR.last_module\": \"SkillTransfer2\",\n" +
                "            \"Call.type\": \"2\",\n" +
                "            \"IVR.error_code\": \"0000000000000000000\",\n" +
                "            \"Call.handle_time\": \"6093\",\n" +
                "            \"Agent.station_type\": \"SOFTPHONE\",\n" +
                "            \"Call.start_timestamp\": \"2022-01-19 16:34:07.937\",\n" +
                "            \"Call.mediatype\": \"voice\",\n" +
                "            \"Call.campaign_name\": \"Omnichannel_Campaign\",\n" +
                "            \"Agent.first_agent\": \"3911840\",\n" +
                "            \"Omni.total_body_chars_size\": \"0\",\n" +
                "            \"Agent.full_name\": \"Sumit Kumar\",\n" +
                "            \"Call.wrapup_time\": \"0\",\n" +
                "            \"Call.skill_id\": \"266184\",\n" +
                "            \"Call.bill_time\": \"12000\",\n" +
                "            \"Call.DNIS\": \"7122214593\",\n" +
                "            \"Call.hold_time\": \"0\",\n" +
                "            \"Agent.id\": \"3911840\",\n" +
                "            \"Call.length\": \"10529\",\n" +
                "            \"Call.domain_id\": \"130736\",\n" +
                "            \"IVR.error_desc\": \"No error\",\n" +
                "            \"Call.ANI\": \"7122215936\",\n" +
                "            \"Agent.user_name\": \"sumit@observeai.partner\",\n" +
                "            \"Call.park_time\": \"0\",\n" +
                "            \"Call.language\": \"en-US\",\n" +
                "            \"Agent.station_id\": \"2676119\",\n" +
                "            \"Omni.total_body_bytes_size\": \"0\",\n" +
                "            \"Call.queue_time\": \"1489\",\n" +
                "            \"Call.number\": \"7122215936\",\n" +
                "            \"Omni.email_priority\": \"0\",\n" +
                "            \"Call.skill_name\": \"Omnichannel_Skill\",\n" +
                "            \"Call.campaign_id\": \"1137587\",\n" +
                "            \"Call.domain_name\": \"ObserveAI\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
        try {
            return new ObjectMapper().readTree(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFive9BaseUrl() {
        return "https://localhost:8080/voicestream/v2";
    }

    public static String getFive9OauthBaseUrl() {
        return "https://localhost:8080";
    }
}
