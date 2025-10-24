package com.example.auth_service.service;

import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.LoginResult;
import com.example.auth_service.dto.SignupRequest;
import com.example.auth_service.dto.TokenPair;

public interface AuthService {
    LoginResult signup(SignupRequest req);
    LoginResult login(LoginRequest req);
    TokenPair refresh (String refreshPlain);
    void logout(String refreshPlain);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
