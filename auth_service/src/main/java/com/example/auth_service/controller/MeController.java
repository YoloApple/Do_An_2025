package com.example.auth_service.controller;

import com.example.auth_service.dto.ProfileResponse;
import com.example.auth_service.dto.ProfileUpdateReq;
import com.example.auth_service.service.MeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {
    private final MeService meService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ProfileResponse getMe(Authentication auth) {
        return meService.getProfile(auth.getName());
    }

    @PatchMapping
    @PreAuthorize("isAuthenticated()")
    public ProfileResponse updateMe(Authentication auth,
                                    @Valid @RequestBody ProfileUpdateReq req) {
        return meService.updateProfile(auth.getName(), req);
    }
}
