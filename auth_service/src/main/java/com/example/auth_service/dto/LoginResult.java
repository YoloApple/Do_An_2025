package com.example.auth_service.dto;

import com.example.auth_service.entity.User;

public record LoginResult(User user, TokenPair tokenPair) {}
