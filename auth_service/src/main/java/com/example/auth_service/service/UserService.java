package com.example.auth_service.service;

import com.example.auth_service.dto.ProfileResponse;
import com.example.auth_service.dto.ProfileUpdateReq;

public interface UserService {
    ProfileResponse getProfile(String username);
    ProfileResponse updateProfile(String username, ProfileUpdateReq req);
}
