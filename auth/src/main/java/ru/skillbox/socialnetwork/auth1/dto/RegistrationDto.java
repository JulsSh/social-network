package ru.skillbox.socialnetwork.auth1.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationDto(
        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(min = 8)
        String password1,

        @NotBlank
        @Size(min = 8)
        String password2,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        String captchaCode
) {
}
