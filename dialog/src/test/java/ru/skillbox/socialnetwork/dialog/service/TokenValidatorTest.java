package ru.skillbox.socialnetwork.dialog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import ru.skillbox.socialnetwork.dialog.config.TokenValidator;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenValidatorTest {

    @Mock
    private RestClient restClient;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private TokenValidator validator;

    @Test
    void returnEmptyWhenTokenInvalid() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Boolean.class)).thenReturn(false);

        Optional<UUID> result = validator.validate("any.token.here");

        assertTrue(result.isEmpty());
    }

    @Test
    void returnEmptyWhenAuthServiceDown() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("connection refused"));

        Optional<UUID> result = validator.validate("a.b.c");

        assertTrue(result.isEmpty());
    }

    @Test
    void returnUserIdWhenTokenValid() throws Exception {
        UUID userId = UUID.randomUUID();

        String payloadJson = "{\"sub\":\"" + userId + "\"}";
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes());
        String token = "header." + payloadBase64 + ".signature";

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Boolean.class)).thenReturn(true);

        when(mapper.readTree(payloadJson)).thenReturn(new ObjectMapper().readTree(payloadJson));

        Optional<UUID> result = validator.validate(token);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get());
    }
}
