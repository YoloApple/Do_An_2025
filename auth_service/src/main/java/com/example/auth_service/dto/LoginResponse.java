package com.example.auth_service.dto;

public record LoginResponse(
        Long userId,
        String username,
        String email,
        String accessToken,
        String refreshToken
) {}
