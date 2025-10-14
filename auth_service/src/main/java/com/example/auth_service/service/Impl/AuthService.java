package com.example.auth_service.service.Impl;

import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.LoginResult;
import com.example.auth_service.dto.SignupRequest;
import com.example.auth_service.dto.TokenPair;
import com.example.auth_service.entity.User;

public interface AuthService {
    User signup(SignupRequest req);
    LoginResult login(LoginRequest req);
    TokenPair refresh (String refreshPlain);
    void logout(String refreshPlain);
}
