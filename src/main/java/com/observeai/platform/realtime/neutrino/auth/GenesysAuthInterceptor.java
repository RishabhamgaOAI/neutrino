package com.observeai.platform.realtime.neutrino.auth;

import com.observeai.platform.realtime.neutrino.util.Constants;
import com.observeai.platform.realtime.neutrino.util.DravityRequestUtil;
import com.observeai.platform.realtime.neutrino.util.ObjectMapperFactory;
import com.observeai.platform.realtime.neutrino.data.dto.AccountInfoWithVendorDetailsDto;
import com.observeai.platform.realtime.neutrino.util.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observeai.platform.realtime.neutrino.data.dto.GenesysIntegrationConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GenesysAuthInterceptor implements HandshakeInterceptor {

    private static final String HMAC_SHA256 = Constants.SIGNATURE_ALGORITHM;

    private final DravityRequestUtil dravityRequestUtil;
    private final StringEncryptor jasyptStringEncryptor;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            String vendorAccountId = getHeaderValue(request, "audiohook-organization-id");
            log.info("received genesys connection request for vendorAccountId={}", vendorAccountId);

            if (vendorAccountId == null ) {
                return unauthorizedResponse(response, "missing required headers for authentication.");
            }

            Optional<HttpResponse<AccountInfoWithVendorDetailsDto>> optionalHttpResponse = fetchAccountInfo(vendorAccountId);
            if (optionalHttpResponse.isEmpty()) {
                return false; // Reject the stream if the account info could not be fetched
            }

            AccountInfoWithVendorDetailsDto accountInfo = optionalHttpResponse.get().getResponse();
            Optional<GenesysIntegrationConfig> genesysConfig = Optional.ofNullable(accountInfo)
                    .map(AccountInfoWithVendorDetailsDto::getVendorAccountDetails)
                    .map(AccountInfoWithVendorDetailsDto.VendorAccountDetailsDto::getConfig)
                    .map(AccountInfoWithVendorDetailsDto.VendorAccountConfig::getGenesysConfig);

            if (genesysConfig.isEmpty()) {
                log.info("genesysConfig is empty for vendorAccountId={}. skipping authentication", vendorAccountId);
                return true; // Skip authentication if genesysConfig is missing
            }

            boolean authenticationEnabled = genesysConfig.get().isAudioStreamAuthenticationEnabled();
            if (!authenticationEnabled) {
                log.info("genesys audio stream authentication is disabled for vendorAccountId={}. skipping authentication", vendorAccountId);
                return true;
            }

            String encryptedSecretKey = genesysConfig.get().getGenesysStreamSecretKey();
            String secretKey = jasyptStringEncryptor.decrypt(encryptedSecretKey);

            String observeAccountId = getHeaderValue(request, "x-api-key");
            String signature = getHeaderValue(request, "signature");
            String signatureInput = getHeaderValue(request, "signature-input");

            if (!observeAccountId.equals(accountInfo.getObserveAccountId())) {
                return unauthorizedResponse(response, "x-api-key validation failed.");
            }

            if (!validateSignature(signature, signatureInput, secretKey, request)) {
                return unauthorizedResponse(response, "signature validation failed.");
            }
            return true; // Authentication successful.
        } catch (Throwable th) {
            log.error("unhandled exception during genesys auth verification. request={}, error={}", request, th.toString(), th);
            return false;
        }
    }

    private Optional<HttpResponse<AccountInfoWithVendorDetailsDto>> fetchAccountInfo(String vendorAccountId) {
        try {
            HttpResponse<AccountInfoWithVendorDetailsDto> httpResponse = dravityRequestUtil.getAccountInfo("Genesys", vendorAccountId);
            if (httpResponse != null && !httpResponse.hasError()) {
                return Optional.of(httpResponse); // Wrap the response in an Optional
            }
        } catch (Throwable e) {
            log.error("error fetching account details for vendorAccountId={}, error={}", vendorAccountId, e.toString(), e);
        }
        return Optional.empty(); // Return an empty Optional in case of failure
    }

    private boolean unauthorizedResponse(ServerHttpResponse response, String message) {
        log.warn(message);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    private String getHeaderValue(ServerHttpRequest request, String headerName) {
        return request.getHeaders().getFirst(headerName);
    }

    private boolean validateSignature(String signature, String signatureInput, String secretKey, ServerHttpRequest request) {
        try {
            String dataToSign = buildDataToSign(signatureInput, request);
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            String generatedSignature = generateHmacSignature(dataToSign, keyBytes);
            return signature.equals("sig1=:" + generatedSignature + ":");
            
        } catch (Exception e) {
            log.error("error during signature validation={}", e.toString(), e);
            return false;
        }
    }

    private String buildDataToSign(String signatureInput, ServerHttpRequest request) {
        String[] parts = signatureInput.split(";");
        String[] components = extractComponents(parts[0]);
        String created = extractParameter(parts, "created=");
        String expires = extractParameter(parts, "expires=");
        String nonce = extractParameter(parts, "nonce=");
        String keyId = extractParameter(parts, "keyid=");
        String algorithm = "hmac-sha256";

        StringBuilder dataToSign = new StringBuilder();
        for (String component : components) {
            String value = getValueForComponent(component, request);
            if (value == null) throw new IllegalArgumentException("Missing value for component: " + component);
            dataToSign.append("\"").append(component).append("\": ").append(value).append("\n");
        }

        dataToSign.append("\"@signature-params\": (")
                .append("\"").append(String.join("\" \"", components)).append("\");")
                .append("created=").append(created).append(";")
                .append("expires=").append(expires).append(";")
                .append("keyid=\"").append(keyId).append("\";")
                .append("nonce=\"").append(nonce).append("\";")
                .append("alg=\"").append(algorithm).append("\"");

        return dataToSign.toString();
    }

    private String[] extractComponents(String componentString) {
        return componentString.split("=")[1].replaceAll("[()\"]", "").split(" ");
    }

    private String extractParameter(String[] parts, String prefix) {
        return java.util.Arrays.stream(parts)
                .filter(part -> part.startsWith(prefix))
                .map(part -> part.split("=")[1].replace("\"", ""))
                .findFirst()
                .orElse(null);
    }

    private String getValueForComponent(String component, ServerHttpRequest request) {
        switch (component) {
            case "@request-target":
                return request.getURI().getPath();
            case "audiohook-session-id":
            case "audiohook-organization-id":
            case "audiohook-correlation-id":
            case "x-api-key":
                return getHeaderValue(request, component);
            case "@authority":
                return getHeaderValue(request, HttpHeaders.HOST);
            default:
                return null;
        }
    }

    public static String generateHmacSignature(String data, byte[] keyBytes) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(keyBytes, HMAC_SHA256));
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No post-handshake logic needed.
    }
}