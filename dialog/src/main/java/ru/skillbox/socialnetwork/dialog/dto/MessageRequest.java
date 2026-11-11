package ru.skillbox.socialnetwork.dialog.dto;


import jakarta.validation.Valid;

import java.util.UUID;

public record MessageRequest(
        String type,
        UUID recipientId,
        @Valid
        MessageDataRequest data) {
}