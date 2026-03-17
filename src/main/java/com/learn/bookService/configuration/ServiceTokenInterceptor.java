package com.learn.bookService.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

// ── NEW FILE ──────────────────────────────────────────────────────────────────
// This interceptor automatically adds a service-to-service JWT to every
// Feign call BookService makes to PricingService.
//
// Flow:
// 1. BookService requests a CLIENT_CREDENTIALS token from Auth Server
// 2. Token is cached until it expires (5 min)
// 3. Every Feign call to PricingService includes: Authorization: Bearer <token>
// 4. PricingService validates the token using Auth Server's JWKS endpoint
// ─────────────────────────────────────────────────────────────────────────────
@Component
public class ServiceTokenInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ServiceTokenInterceptor.class);

    @Value("${auth.server.token-uri}")
    private String tokenUri;   // http://auth-server:7001/oauth2/token

    @Value("${auth.server.client-id}")
    private String clientId;   // book-service

    @Value("${auth.server.client-secret}")
    private String clientSecret; // book-secret

    // ── Token cache ───────────────────────────────────────────────────────────
    private String cachedToken;
    private Instant tokenExpiry;

    @Override
    public void apply(RequestTemplate template) {
        String token = getValidToken();
        template.header("Authorization", "Bearer " + token);
    }

    // ── Returns cached token or fetches a new one if expired ─────────────────
    private synchronized String getValidToken() {
        if (cachedToken == null || Instant.now().isAfter(tokenExpiry.minusSeconds(30))) {
            // Token is expired or about to expire - fetch a new one
            logger.debug("Fetching new service token from Auth Server");
            fetchNewToken();
        }
        return cachedToken;
    }

    // ── Fetches CLIENT_CREDENTIALS token from Auth Server ────────────────────
    @SuppressWarnings("unchecked")
    private void fetchNewToken() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // OAuth2 client_credentials request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(clientId, clientSecret); // Basic auth with client credentials

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("scope", "internal");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);

            Map<String, Object> tokenResponse = response.getBody();
            cachedToken = (String) tokenResponse.get("access_token");
            int expiresIn = (int) tokenResponse.get("expires_in");
            tokenExpiry = Instant.now().plusSeconds(expiresIn);

            logger.info("Successfully fetched new service token, expires in {}s", expiresIn);

        } catch (Exception e) {
            logger.error("Failed to fetch service token from Auth Server: {}", e.getMessage());
            throw new RuntimeException("Could not obtain service token from Auth Server", e);
        }
    }
}