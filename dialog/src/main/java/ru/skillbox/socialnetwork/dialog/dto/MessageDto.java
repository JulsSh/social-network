package ru.skillbox.socialnetwork.dialog.dto;

import ru.skillbox.socialnetwork.dialog.enums.ReadStatus;

import java.time.Instant;
import java.util.UUID;

public record MessageDto(
        UUID id,
        UUID dialogId,
        Instant time,
        String messageText,
        UUID conversationPartner1,
        UUID conversationPartner2,
        ReadStatus readStatus,
        boolean stubDate,
        String date,
        UUID authorId) {
}