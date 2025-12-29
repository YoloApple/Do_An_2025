package com.example.auth_service.controller;

import com.example.auth_service.dto.ApiResponse;
import com.example.auth_service.dto.ProfileResponse;
import com.example.auth_service.dto.ProfileUpdateReq;
import com.example.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMe(Authentication auth) {
        ProfileResponse profile = userService.getProfile(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    @PatchMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateMe(Authentication auth,
                                    @Valid @RequestBody ProfileUpdateReq req) {
        ProfileResponse updatedProfile = userService.updateProfile(auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success(updatedProfile, "Profile updated successfully"));
    }
}
