package ru.skillbox.socialnetwork.dialog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record GetDialogsRs(
        String error,

        @JsonProperty("error_description")
        String errorDescription,
        Long timestamp,
        Long total,
        Long offset,
        @JsonProperty("per_page")
        Integer perPage,
        UUID id,
        List<DialogDto> content) {
}