package com.example.auth_service.controller;

import com.example.auth_service.dto.*;
import com.example.auth_service.entity.User;
import com.example.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<LoginResponse>> signup(@Valid @RequestBody SignupRequest req) {
        LoginResult result = authService.signup(req);
        User user = result.user();
        TokenPair pair = result.tokenPair();

        LoginResponse loginResponse = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                pair.accessToken(),
                pair.refreshToken()
        );

        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Signup and login successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        LoginResult result = authService.login(req);
        User user = result.user();
        TokenPair pair = result.tokenPair();

        LoginResponse loginResponse = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                pair.accessToken(),
                pair.refreshToken()
        );

        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenPair>> refresh(@Valid @RequestBody TokenRequest req) {
        TokenPair pair = authService.refresh(req.token());
        return ResponseEntity.ok(ApiResponse.success(pair, "Token refreshed successfully"));
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody TokenRequest req) {
        authService.logout(req.token());
        return ResponseEntity.noContent().build();
    }
}

