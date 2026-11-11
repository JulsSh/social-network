package ru.skillbox.socialnetwork.dialog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.skillbox.socialnetwork.dialog.enums.NotificationType;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private UUID notificationId;
    private UUID userId;
    private UUID authorId;
    private String content;
    private NotificationType notificationType;
    private Long timestamp;
}