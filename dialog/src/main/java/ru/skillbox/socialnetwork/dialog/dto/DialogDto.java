package ru.skillbox.socialnetwork.dialog.dto;

import java.util.UUID;

public record DialogDto(
        UUID id,
        Long unreadCount,
        UUID conversationPartner1,
        UUID conversationPartner2,
        MessageDto lastMessage) {
}