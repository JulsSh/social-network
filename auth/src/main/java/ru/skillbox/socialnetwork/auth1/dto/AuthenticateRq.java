package ru.skillbox.socialnetwork.auth1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticateRq(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password) {
}
