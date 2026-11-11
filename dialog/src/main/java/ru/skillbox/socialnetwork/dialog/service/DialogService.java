package ru.skillbox.socialnetwork.dialog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.socialnetwork.dialog.dto.*;
import ru.skillbox.socialnetwork.dialog.entity.Dialog;
import ru.skillbox.socialnetwork.dialog.entity.Message;
import ru.skillbox.socialnetwork.dialog.enums.ReadStatus;
import ru.skillbox.socialnetwork.dialog.exception.DialogNotFoundException;
import ru.skillbox.socialnetwork.dialog.repository.DialogRepository;
import ru.skillbox.socialnetwork.dialog.repository.MessageRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RequiredArgsConstructor
@Service
public class DialogService {
    private final DialogRepository dialogRepository;
    private final MessageRepository messageRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SetStatusMessageReadRs setDialogAsRead(UUID dialogId, UUID receiverId) {
        dialogRepository.findById(dialogId)
                .orElseThrow(() -> new DialogNotFoundException(dialogId));
        List<Message> listOfMessages = messageRepository.findByDialogIdAndReceiverIdAndReadStatus(dialogId, receiverId, ReadStatus.SENT);
        for (Message message : listOfMessages) {
            message.setReadStatus(ReadStatus.READ);
        }
        return new SetStatusMessageReadRs(null, System.currentTimeMillis(), new SetStatusMessageReadDto("ok"), null);
    }

    public MessageDto toMessageDto(Message m, Dialog d) {
        return new MessageDto(m.getId(), d.getId(), m.getTime(), m.getMessageText(), m.getSenderId(), m.getReceiverId(),
                m.getReadStatus(), false, null, m.getSenderId());
    }

    @Transactional(readOnly = true)
    public GetDialogsRs getDialogsRs(UUID currentUserId, Pageable pageable) {
        Page<Dialog> dialogs = dialogRepository.findByConversationPartner1OrConversationPartner2(currentUserId, currentUserId, pageable);
        List<DialogDto> dtoToList = new ArrayList<>();
        for (Dialog dialog : dialogs) {
            long unreadCountForCurrentUser = messageRepository.countByDialogIdAndReceiverIdAndReadStatus(dialog.getId(), currentUserId, ReadStatus.SENT);
            Optional<Message> message = messageRepository.findFirstByDialogIdOrderByTimeDesc(dialog.getId());
            MessageDto lastMessageDto = null;
            if (message.isPresent()) {
                Message m = message.get();
                lastMessageDto = toMessageDto(m, dialog);
            }
            DialogDto dto = new DialogDto(dialog.getId(), unreadCountForCurrentUser, dialog.getConversationPartner1(), dialog.getConversationPartner2(), lastMessageDto);
            dtoToList.add(dto);
        }
        return new GetDialogsRs(null, null, System.currentTimeMillis(), dialogs.getTotalElements(), pageable.getOffset(),
                pageable.getPageSize(), currentUserId, dtoToList);
    }

    @Transactional(readOnly = true)
    public DialogDto getDialogByRecipientId(UUID currentId, UUID recipientId) {
        Optional<Dialog> dialog = dialogRepository.findByConversationPartner1AndConversationPartner2(currentId, recipientId);

        MessageDto lastMessageDto = null;
        if (dialog.isPresent()) {
            Dialog d = dialog.get();
            Optional<Message> message = messageRepository.findFirstByDialogIdOrderByTimeDesc(d.getId());
            Long unreadCount = messageRepository.countByDialogIdAndReceiverIdAndReadStatus(d.getId(), currentId, ReadStatus.SENT);
            if (message.isPresent()) {
                lastMessageDto = toMessageDto(message.get(), d);
            }
            return new DialogDto(d.getId(), unreadCount, d.getConversationPartner1(), d.getConversationPartner2(), lastMessageDto);
        }
        return new DialogDto(null, 0L, currentId, recipientId, null);
    }

    @Transactional(readOnly = true)
    public GetMessagesRs getMessagesByRecipientId(UUID currentUserId, UUID recipientId, Pageable pageable) {
        Optional<Dialog> dialog = dialogRepository.findByConversationPartner1AndConversationPartner2(currentUserId, recipientId);
        if (dialog.isEmpty()) {
            return new GetMessagesRs(null, null, System.currentTimeMillis(), 0L, 0, pageable.getPageSize(), pageable.getOffset(), 0L, List.of());
        }
        Page<Message> messages = messageRepository.findByDialogId(dialog.get().getId(), pageable);
        Long unreadCount = messageRepository.countByDialogIdAndReceiverIdAndReadStatus(dialog.get().getId(), currentUserId, ReadStatus.SENT);
        List<MessageDto> content = new ArrayList<>();
        for (Message message : messages) {
            content.add(toMessageDto(message, dialog.get()));
        }
        return new GetMessagesRs(null, null, System.currentTimeMillis(), messages.getTotalElements(), messages.getTotalPages(),
                pageable.getPageSize(), pageable.getOffset(), unreadCount, content);
    }

    @Transactional(readOnly = true)
    public UnreadCountRs getUnreadCount(UUID currentUserId) {
        UnreadCountDto unreadCountDto = new UnreadCountDto(messageRepository
                .countByReceiverIdAndReadStatus(currentUserId, ReadStatus.SENT));
        return new UnreadCountRs(null, System.currentTimeMillis(), unreadCountDto, null);
    }

    @Transactional
    public MessageDto saveMessage(UUID senderId, UUID recipientId, String text) {
        Optional<Dialog> dialog = dialogRepository
                .findByConversationPartner1AndConversationPartner2(senderId, recipientId);
        Dialog targetDialog;

        if (dialog.isPresent()) {
            targetDialog = dialog.get();
        } else {
            Dialog dialogNew = Dialog.builder()
                    .conversationPartner1(senderId)
                    .conversationPartner2(recipientId)
                    .createdAt(Instant.now())
                    .modifiedAt(Instant.now())
                    .build();
            targetDialog = dialogRepository.save(dialogNew);
        }
        Message targetMessage = Message.builder()
                .dialog(targetDialog)
                .time(Instant.now())
                .messageText(text)
                .senderId(senderId)
                .receiverId(recipientId)
                .readStatus(ReadStatus.SENT)
                .build();
        messageRepository.save(targetMessage);
        targetDialog.setModifiedAt(Instant.now());
        MessageDto saved = toMessageDto(targetMessage, targetDialog);
        eventPublisher.publishEvent(new MessageSavedEvent(saved));
        return saved;
    }
}