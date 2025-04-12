package com.observeai.platform.realtime.neutrino.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newrelic.api.agent.NewRelic;
import com.observeai.authorization.client.dto.AuthContextDTO;
import com.observeai.authorization.client.exception.AuthzClientExceptionCode;
import com.observeai.authorization.client.exception.AuthzClientRuntimeException;
import com.observeai.authorization.client.utils.CompressionUtil;
import com.observeai.platform.realtime.neutrino.config.AuthConfig;
import com.observeai.platform.realtime.neutrino.data.dto.AccountAndUserInfoResponseDto;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto;
import com.observeai.platform.realtime.neutrino.data.dto.NiceEventDto;
import com.observeai.platform.realtime.neutrino.data.dto.NiceIntegrationConfig;
import com.observeai.platform.realtime.neutrino.exception.neutrino.AuthenticationFailureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private final AuthConfig authConfig;
    private static final String AUTH_CONTEXT_HEADER_KEY = "auth-context";

    public AuthContextDTO authenticate(String authHeader) {
        try {
            if (authHeader != null) {
                String serviceUrl = authConfig.getBaseUrl() + authConfig.getVerifyPath();
                HttpHeaders headers = headerWithApiAuth(authConfig);
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (authHeader.startsWith("Basic")) {
                    String[] split = authHeader.split(" ");
                    headers.setBasicAuth(split[1]);
                } else {
                    headers.setBasicAuth(authHeader);
                }
                final ResponseEntity<String> response = REST_TEMPLATE.exchange(
                        serviceUrl, HttpMethod.POST, new HttpEntity<>(headers), String.class);
                AuthContextDTO authContextDTO = buildAuthContext(Objects.requireNonNull(response.getHeaders().get(AUTH_CONTEXT_HEADER_KEY)));
                return authContextDTO;
            }
        }
        catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("authentication failed with error. errorCode={}", ex.getStatusCode());
        }
        throw new AuthenticationFailureException("authorization header is not present or not valid");
    }

    public void verifyAuth(String authHeader, String deploymentCluster) {
        String host = "BILL".equals(deploymentCluster) ? authConfig.getRestrictedAuthHost() : authConfig.getAuthHost();
        String verifyPathUrl = host + authConfig.getVerifyPath();

        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new AuthenticationFailureException("authorization header is not present");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + authHeader);
            HttpEntity<String> requestEntity = new HttpEntity<>("{}", headers);
            ResponseEntity<String> response = REST_TEMPLATE.exchange(verifyPathUrl, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().isError()) {
                throw new AuthenticationFailureException("authentication failed with status={}" + response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("authentication failed. status={}", ex.getStatusCode());
            NewRelic.noticeError(ex);
            throw new AuthenticationFailureException("authentication failed={} " + ex.toString());
        } catch (Exception ex) {
            log.error("unexpected error during authentication={}", ex.toString(), ex);
            NewRelic.noticeError(ex);
            throw new AuthenticationFailureException("authentication failed due to unexpected error={}" + ex.toString());
        }
    }

    public boolean authenticateNiceEvent(NiceEventDto niceEventDto, AccountAndUserInfoResponseDto accountAndUserInfo) {
        try {
            Optional<NiceIntegrationConfig> niceConfig = Optional.ofNullable(accountAndUserInfo.getAccountInfo())
                    .map(AccountInfoWithVendorDetailsDto::getVendorAccountDetails)
                    .map(AccountInfoWithVendorDetailsDto.VendorAccountDetailsDto::getConfig)
                    .map(AccountInfoWithVendorDetailsDto.VendorAccountConfig::getNiceConfig);

            if (niceConfig.isEmpty()) {
                log.info("Nice config is not present for vendorAccountId={}, vendorCallId={}. Skipping authentication.", niceEventDto.getAccountId(), niceEventDto.getContactId());
                return true;
            } 
            
            if (!niceConfig.get().isAudioStreamAuthenticationEnabled()) {
                log.info("Audio stream authentication is disabled for vendorAccountId={}, vendorCallId={}. Skipping authentication.", niceEventDto.getAccountId(), niceEventDto.getContactId());
                return true;
            } 
            String authToken = extractAuthToken(niceEventDto.getAdditionalParams());
            if (authToken == null || authToken.trim().isEmpty()) {
                log.error("Authentication token is null or empty for vendorCallId={}, vendorAccountId={}", niceEventDto.getContactId(), niceEventDto.getAccountId());
                return false;
            }
            
            verifyAuth(authToken, accountAndUserInfo.getAccountInfo().getDeploymentCluster());
        
        } catch (AuthenticationFailureException e) {
            log.error("Authentication failed for vendorCallId={}, vendorAccountId={} with error={}", niceEventDto.getContactId(), niceEventDto.getAccountId(), e.toString());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during authentication for vendorCallId={}, vendorAccountId={} with error={}", niceEventDto.getContactId(), niceEventDto.getAccountId(), e.toString());
            return false;
        }
        
        return true;
    }

    private HttpHeaders headerWithApiAuth(AuthConfig authConfig) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("app_id", authConfig.getAppId());
        headers.add("app_alias", authConfig.getAssetAlias());
        headers.add("app_secret", authConfig.getAppSecret());
        return headers;
    }

    private AuthContextDTO buildAuthContext(List<String> authContextHeader) {
        return authContextHeader.stream()
                .filter(StringUtils::hasLength)
                .map(AuthenticationService::deCompressRoleString)
                .map(AuthenticationService::deserializeRole)
                .findAny().orElse(AuthContextDTO.builder().build());
    }

    private static String deCompressRoleString(String roleString) {
        try {
            return CompressionUtil.decompressB64(roleString);
        } catch (IOException e) {
            log.error("invalid compressed role string: {}", roleString, e);
            throw new AuthzClientRuntimeException(AuthzClientExceptionCode.DECOMPRESSION_EXCEPTION, e);
        }
    }

    private static AuthContextDTO deserializeRole(String roleJson)
            throws AuthzClientRuntimeException {
        try {
            return MAPPER.readValue(roleJson, AuthContextDTO.class);
        } catch (JsonProcessingException e) {
            log.error("invalid role json: {}", roleJson, e);
            throw new AuthzClientRuntimeException(AuthzClientExceptionCode.JSON_PARSE_EXCEPTION, e);
        }
    }

    private String extractAuthToken(String additionalParams) {
        if (additionalParams == null || additionalParams.trim().isEmpty()) {
            return null;
        }

        try {
            JSONObject jsonObject = new JSONObject(additionalParams);
            return jsonObject.optString("authToken", null);
        } catch (Exception e) {
            log.error("Failed to parse additionalParams JSON: {}", e.toString());
            return null;
        }
    }

}
