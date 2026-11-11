package ru.skillbox.socialnetwork.dialog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.skillbox.socialnetwork.dialog.entity.Message;
import ru.skillbox.socialnetwork.dialog.enums.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByDialogId(UUID id, Pageable pageable);

    List<Message> findByDialogIdAndReceiverIdAndReadStatus(UUID uuid, UUID receiverId, ReadStatus readStatus);

    Long countByDialogIdAndReceiverIdAndReadStatus(UUID uuid, UUID receiverId, ReadStatus readStatus);

    Optional<Message> findFirstByDialogIdOrderByTimeDesc(UUID dialogId);

    Long countByReceiverIdAndReadStatus(UUID currentUserId, ReadStatus readStatus);
}