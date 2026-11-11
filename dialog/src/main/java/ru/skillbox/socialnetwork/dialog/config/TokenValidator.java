package ru.skillbox.socialnetwork.dialog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenValidator {
    @Value("${auth.service.url}")
    private String authServiceUrl;
    private final RestClient restClient;
    private final ObjectMapper mapper;

    public Optional<UUID> validate(String token) {
        try {
            Boolean valid = restClient.get().uri(authServiceUrl + "/api/v1/auth/validate?token=" + token).retrieve().body(Boolean.class);
            if (valid != null && valid) {
                String[] parts = token.split("\\.");
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                UUID userId = UUID.fromString(mapper.readTree(payload).get("sub").asText());
                return Optional.of(userId);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            log.warn("Token validation failed " + e.getMessage());
            return Optional.empty();
        }
    }
}
