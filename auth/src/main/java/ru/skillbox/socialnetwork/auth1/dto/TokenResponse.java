package ru.skillbox.socialnetwork.auth1.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
