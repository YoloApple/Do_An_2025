package com.example.auth_service.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String token, String username);
}
