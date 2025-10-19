package com.example.auth_service.dto;

import com.example.auth_service.entity.Gender;

public record ProfileResponse(
        Long id,
        String username,
        String email,
        String phone,
        Gender gender
){}
