package com.example.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(min=6) String username,
        @Email @NotBlank String email,
        @NotBlank @Size(min=8) String password
) {}


