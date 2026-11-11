package ru.skillbox.socialnetwork.dialog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.skillbox.socialnetwork.dialog.dto.MessageDto;
import ru.skillbox.socialnetwork.dialog.dto.MessageSavedEvent;
import ru.skillbox.socialnetwork.dialog.dto.NotificationEvent;
import ru.skillbox.socialnetwork.dialog.enums.NotificationType;
import ru.skillbox.socialnetwork.dialog.enums.ReadStatus;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private NotificationListener listener;

    @Test
    void buildNotificationFromSavedMessage() {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        Instant time = Instant.now();

        MessageDto message = new MessageDto(messageId, UUID.randomUUID(), time, "hello",
                senderId, recipientId, ReadStatus.SENT, false, null, senderId);

        listener.onMessageSend(new MessageSavedEvent(message));

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationProducer).send(captor.capture());

        NotificationEvent event = captor.getValue();
        assertEquals(recipientId, event.getUserId());
        assertEquals(senderId, event.getAuthorId());
        assertEquals("hello", event.getContent());
        assertEquals(NotificationType.MESSAGE, event.getNotificationType());
    }
}