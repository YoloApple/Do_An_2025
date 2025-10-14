package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id") private User user;
    @Column(nullable = false, unique = true) private String tokenHash;
    @Column(nullable = false)private Instant expiresAt;
    @Column(nullable = false)private boolean revoked =false;
    private Long replacedBy;
    private Instant createdAt = Instant.now();
}
