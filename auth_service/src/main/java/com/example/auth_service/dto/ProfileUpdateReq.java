package com.example.auth_service.dto;

import com.example.auth_service.entity.Gender;
import jakarta.validation.constraints.Pattern;

public record ProfileUpdateReq(
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone must be E.164 digits")
        String phone,
        Gender gender,
        String description
){}
