package ru.skillbox.socialnetwork.dialog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.skillbox.socialnetwork.dialog.dto.MessageDto;
import ru.skillbox.socialnetwork.dialog.dto.MessageSavedEvent;
import ru.skillbox.socialnetwork.dialog.dto.NotificationEvent;
import ru.skillbox.socialnetwork.dialog.enums.NotificationType;

@Component
@RequiredArgsConstructor
public class NotificationListener {
    private final NotificationProducer notificationProducer;
    @TransactionalEventListener
          public void onMessageSend(MessageSavedEvent event) {
        MessageDto message = event.message();
        notificationProducer.send(NotificationEvent.builder()
                .notificationId(message.id())
                .userId(message.conversationPartner2())
                .authorId(message.authorId())
                .content(message.messageText())
                .notificationType(NotificationType.MESSAGE)
                .timestamp(message.time().toEpochMilli())
                .build());
    }
}
