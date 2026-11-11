package ru.skillbox.socialnetwork.dialog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "dialogs")
public class Dialog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID conversationPartner1;
    private UUID conversationPartner2;
    private Instant createdAt;
    private Instant modifiedAt;
}