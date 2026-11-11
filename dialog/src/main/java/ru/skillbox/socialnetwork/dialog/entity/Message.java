package ru.skillbox.socialnetwork.dialog.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.skillbox.socialnetwork.dialog.enums.ReadStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "messages")
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Dialog dialog;

    @Column(name = "sent_time")
    private Instant time;

    private String messageText;

    @Column(name = "sender_id")
    private UUID senderId;

    @Column(name = "receiver_id")
    private UUID receiverId;

    @Enumerated(EnumType.STRING)
    private ReadStatus readStatus;
}