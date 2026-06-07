package com.haal.backend.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "anonymous_alias", nullable = false)
    private String anonymousAlias;

    @Column(name = "phone_hash", nullable = false)
    private String phoneHash;

    @Column(name = "phone_encrypted", nullable = false)
    private String phoneEncrypted;

    @Column(name = "ec_name_encrypted", nullable = false)
    private String ecNameEncrypted;

    @Column(name = "ec_phone_encrypted", nullable = false)
    private String ecPhoneEncrypted;

    @Column(name = "ec_email_encrypted")
    private String ecEmailEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
