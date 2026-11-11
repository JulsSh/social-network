package ru.skillbox.socialnetwork.auth1.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.skillbox.socialnetwork.auth1.Role;

import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", nullable = false, length = 255)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 255)
    private String lastName;
    private Instant createdAt;
    private Instant modifiedAt;

    @Column(name = "role", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Role role;
}
