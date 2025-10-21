package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;

@Entity
@Data
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    @Column(unique = true,nullable = false)
    private String email;
    private boolean enabled = true;
    private Instant createdAt = Instant.now();
    @Column(length = 20,unique = true)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;
}
