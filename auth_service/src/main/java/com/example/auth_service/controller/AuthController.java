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

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req.email());
        return ResponseEntity.ok(ApiResponse.success("Password reset link has been sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.token(), req.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody TokenRequest req) {
        authService.logout(req.token());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/oauth2/exchange")
    public ResponseEntity<ApiResponse<LoginResponse>> exchangeOAuth2Code(@RequestParam String code) {
        // 1. Lấy token từ cache/session dựa vào code
        LoginResult result = authService.getOAuth2TokenByCode(code);
        
        // 2. Xóa code để tránh reuse
        authService.deleteOAuth2Code(code);
        
        User user = result.user();
        TokenPair pair = result.tokenPair();
        LoginResponse response = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                pair.accessToken(),
                pair.refreshToken()
        );
        return ResponseEntity.ok(ApiResponse.success(response, "OAuth2 login successful"));
    }
}

