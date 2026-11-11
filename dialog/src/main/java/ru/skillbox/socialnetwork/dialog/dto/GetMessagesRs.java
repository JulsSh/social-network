package ru.skillbox.socialnetwork.dialog.dto;

import java.util.List;

public record GetMessagesRs(
        String error,
        String errorDescription,
        Long timestamp,
        Long total,
        Integer totalPages,
        Integer perPage,
        Long offset,
        Long unreadCount,
        List<MessageDto> content) {
}