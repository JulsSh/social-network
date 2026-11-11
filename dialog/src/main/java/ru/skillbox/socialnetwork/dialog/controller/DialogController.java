package ru.skillbox.socialnetwork.dialog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.socialnetwork.dialog.dto.*;
import ru.skillbox.socialnetwork.dialog.service.DialogService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/dialogs")
public class DialogController {
    private final DialogService dialogService;

    @PutMapping("/{dialogId}")
    public ResponseEntity<SetStatusMessageReadRs> setStatusMessageRead(
            @PathVariable UUID dialogId,
            @AuthenticationPrincipal UUID currentUserId) {
        SetStatusMessageReadRs body = dialogService.setDialogAsRead(dialogId, currentUserId);
        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<GetDialogsRs> getDialogsRs(@AuthenticationPrincipal UUID currentUserId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        GetDialogsRs body = dialogService.getDialogsRs(currentUserId, PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "modifiedAt")));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/unread")
    public ResponseEntity<UnreadCountRs> getUnreadDialogs(@AuthenticationPrincipal UUID currentUserId) {
        UnreadCountRs unread = dialogService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(unread);
    }

    @GetMapping("/recipientId/{recipientId}")
    public ResponseEntity<DialogDto> getDialogByRecipientId(@AuthenticationPrincipal UUID currentUserId,
                                                            @PathVariable UUID recipientId) {
        DialogDto body = dialogService.getDialogByRecipientId(currentUserId, recipientId);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/messages")
    public ResponseEntity<GetMessagesRs> getMessagesByRecipientId(@AuthenticationPrincipal UUID currentUserId,
                                                                  @RequestParam UUID recipientId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size) {
        GetMessagesRs body = dialogService.getMessagesByRecipientId(currentUserId, recipientId, PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "time", "id")));
        return ResponseEntity.ok(body);
    }
}