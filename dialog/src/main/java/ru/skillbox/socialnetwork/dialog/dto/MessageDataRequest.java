package ru.skillbox.socialnetwork.dialog.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.skillbox.socialnetwork.dialog.enums.ReadStatus;

import java.time.Instant;
import java.util.UUID;

public record MessageDataRequest(
        Instant time,
        UUID conversationPartner1,
        UUID conversationPartner2,
        @NotBlank
        @Size(max = 2048)
        String messageText,
        ReadStatus readStatus,
        UUID id,
        UUID dialogId,
        String clientId) {
}