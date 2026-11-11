package ru.skillbox.socialnetwork.dialog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UnreadCountRs(
        String error,
        Long timestamp,
        UnreadCountDto data,
        @JsonProperty("error_description")
        String errorDescription) {
}