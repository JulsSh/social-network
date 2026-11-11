package ru.skillbox.socialnetwork.dialog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.skillbox.socialnetwork.dialog.entity.Dialog;

import java.util.Optional;
import java.util.UUID;

public interface DialogRepository extends JpaRepository<Dialog, UUID> {
    @Query("select d from Dialog d where (d.conversationPartner1=:a and d.conversationPartner2=:b) or " +
            "(d.conversationPartner1=:b and d.conversationPartner2=:a)")
    Optional<Dialog> findByConversationPartner1AndConversationPartner2(@Param("a") UUID conversationPartner1,
                                                                       @Param("b") UUID conversationPartner2);

    Page<Dialog> findByConversationPartner1OrConversationPartner2(UUID conversationPartner1,
                                                                  UUID conversationPartner2,
                                                                  Pageable pageable);
}