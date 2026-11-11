package ru.skillbox.socialnetwork.dialog.dto;


public record SetStatusMessageReadRs(
        String error,
        Long timestamp,
        SetStatusMessageReadDto data,
        String errorDescription) {
}