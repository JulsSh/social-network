package ru.skillbox.socialnetwork.dialog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.skillbox.socialnetwork.dialog.dto.*;
import ru.skillbox.socialnetwork.dialog.entity.Dialog;
import ru.skillbox.socialnetwork.dialog.entity.Message;
import ru.skillbox.socialnetwork.dialog.enums.ReadStatus;
import ru.skillbox.socialnetwork.dialog.exception.DialogNotFoundException;
import ru.skillbox.socialnetwork.dialog.repository.DialogRepository;
import ru.skillbox.socialnetwork.dialog.repository.MessageRepository;
import static org.mockito.ArgumentMatchers.any;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DialogServiceTest {
    @Mock
    private DialogRepository dialogRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private DialogService service;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void createNewDialogWhenNoneExists() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID dialogId = UUID.randomUUID();
        String text = "hello";

        when(dialogRepository.findByConversationPartner1AndConversationPartner2(senderId, recipientId))
                .thenReturn(Optional.empty());

        Dialog savedDialog = new Dialog();
        savedDialog.setId(dialogId);
        savedDialog.setConversationPartner1(senderId);
        savedDialog.setConversationPartner2(recipientId);
        when(dialogRepository.save(any(Dialog.class))).thenReturn(savedDialog);

        MessageDto result = service.saveMessage(senderId, recipientId, text);

        assertEquals(text, result.messageText());
        assertEquals(dialogId, result.dialogId());
        assertEquals(senderId, result.authorId());
        assertEquals(ReadStatus.SENT, result.readStatus());

        verify(dialogRepository).save(any(Dialog.class));
        verify(messageRepository).save(any(Message.class));
        verify(eventPublisher).publishEvent(any(MessageSavedEvent.class));
    }

    @Test
    void saveMessageToExistingDialog() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID dialogId = UUID.randomUUID();

        Dialog dialog = new Dialog();
        dialog.setId(dialogId);
        dialog.setConversationPartner1(senderId);
        dialog.setConversationPartner2(recipientId);
        String text = "hello";

        when(dialogRepository.findByConversationPartner1AndConversationPartner2(senderId, recipientId))
                .thenReturn(Optional.of(dialog));

        MessageDto result = service.saveMessage(senderId, recipientId, text);
        assertEquals(text, result.messageText());
        assertEquals(dialogId, result.dialogId());
        assertEquals(senderId, result.authorId());
        assertEquals(ReadStatus.SENT, result.readStatus());

        verify(dialogRepository, never()).save(any(Dialog.class));
        verify(messageRepository).save(any(Message.class));
        verify(eventPublisher).publishEvent(any(MessageSavedEvent.class));
    }

    @Test
    void returnEmptyDialogWhenNoDialogExists() {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        when(dialogRepository.findByConversationPartner1AndConversationPartner2(senderId, receiverId))
                .thenReturn(Optional.empty());
        DialogDto result = service.getDialogByRecipientId(senderId, receiverId);
        assertNotNull(result);
        assertNull(result.id());
        assertEquals(senderId, result.conversationPartner1());
        assertEquals(receiverId, result.conversationPartner2());
    }

    @Test
    void returnDialogByRecipientId() {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID dialogId = UUID.randomUUID();

        Dialog dialog = new Dialog();
        dialog.setId(dialogId);
        dialog.setConversationPartner1(senderId);
        dialog.setConversationPartner2(receiverId);
        when(dialogRepository.findByConversationPartner1AndConversationPartner2(senderId, receiverId))
                .thenReturn(Optional.of(dialog));
        when(messageRepository.countByDialogIdAndReceiverIdAndReadStatus(dialogId, senderId, ReadStatus.SENT))
                .thenReturn(3L);

        DialogDto result = service.getDialogByRecipientId(senderId, receiverId);
        assertNotNull(result);
        assertEquals(dialogId, result.id());
        assertEquals(3L, result.unreadCount());
    }

    @Test
    void returnUnreadCount() {
        UUID userId = UUID.randomUUID();
        when(messageRepository.countByReceiverIdAndReadStatus(userId, ReadStatus.SENT))
                .thenReturn(7L);
        UnreadCountRs unreadCountRs = service.getUnreadCount(userId);
        assertEquals(7L, unreadCountRs.data().count());
    }

    @Test
    void returnUnreadCountIsZero() {
        UUID userId = UUID.randomUUID();
        when(messageRepository
                .countByReceiverIdAndReadStatus(userId, ReadStatus.SENT)).thenReturn(0L);
        UnreadCountRs unreadCountRs = service.getUnreadCount(userId);
        assertEquals(0L, unreadCountRs.data().count());
    }

    @Test
    void markMessagesAsRead() {
        UUID dialogId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        Message message = new Message();
        message.setReadStatus(ReadStatus.SENT);

        when(dialogRepository.findById(dialogId)).thenReturn(Optional.of(new Dialog()));
        when(messageRepository.findByDialogIdAndReceiverIdAndReadStatus(dialogId, receiverId, ReadStatus.SENT))
                .thenReturn(List.of(message));

        service.setDialogAsRead(dialogId, receiverId);

        assertEquals(ReadStatus.READ, message.getReadStatus());
    }

    @Test
    void throwWhenDialogNotFound() {
        UUID dialogId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        when(dialogRepository.findById(dialogId)).thenReturn(Optional.empty());

        assertThrows(DialogNotFoundException.class,
                () -> service.setDialogAsRead(dialogId, receiverId));
    }

    @Test
    void getMessagesByRecipientId(){
        UUID currentUserId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        when(dialogRepository.findByConversationPartner1AndConversationPartner2(currentUserId, recipientId))
                .thenReturn(Optional.empty());
        GetMessagesRs result =
                service.getMessagesByRecipientId(currentUserId, recipientId, pageable);
        assertEquals(0L, result.total());
        assertEquals(20, result.perPage());
        assertTrue(result.content().isEmpty());
    }

    @Test
    void returnMessagesWhenDialogExists() {
        UUID currentUserId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID dialogId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        Dialog dialog = new Dialog();
        dialog.setId(dialogId);
        dialog.setConversationPartner1(currentUserId);
        dialog.setConversationPartner2(recipientId);

        Message message = new Message();
        message.setId(messageId);
        message.setMessageText("hello");
        message.setSenderId(recipientId);
        message.setReceiverId(currentUserId);
        message.setReadStatus(ReadStatus.SENT);
        message.setTime(Instant.now());

        when(dialogRepository.findByConversationPartner1AndConversationPartner2(currentUserId, recipientId))
                .thenReturn(Optional.of(dialog));
        when(messageRepository.findByDialogId(dialogId, pageable))
                .thenReturn(new PageImpl<>(List.of(message)));
        when(messageRepository.countByDialogIdAndReceiverIdAndReadStatus(dialogId, currentUserId, ReadStatus.SENT))
                .thenReturn(1L);

        GetMessagesRs result = service.getMessagesByRecipientId(currentUserId, recipientId, pageable);

        assertEquals(1, result.content().size());
        assertEquals(1L, result.unreadCount());

        MessageDto dto = result.content().get(0);
        assertEquals(messageId, dto.id());
        assertEquals(dialogId, dto.dialogId());
        assertEquals("hello", dto.messageText());
        assertEquals(recipientId, dto.authorId());
    }
}