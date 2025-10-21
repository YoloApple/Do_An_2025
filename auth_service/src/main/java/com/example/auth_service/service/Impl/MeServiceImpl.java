package com.example.auth_service.service.Impl;

import com.example.auth_service.dto.ProfileResponse;
import com.example.auth_service.dto.ProfileUpdateReq;
import com.example.auth_service.entity.Gender;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.MeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeServiceImpl implements MeService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String username) {
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toDto(u);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(String username, ProfileUpdateReq req) {
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.phone() != null && !req.phone().isBlank()) {
            if (userRepo.existsByPhoneAndIdNot(req.phone(), u.getId())) {
                throw new RuntimeException("Phone already in use");
            }
            u.setPhone(req.phone().trim());
        } else {
            u.setPhone(null);
        }

        u.setGender(req.gender() != null ? req.gender() : Gender.UNKNOWN);
        userRepo.save(u);
        return toDto(u);
    }

    private ProfileResponse toDto(User u) {
        return new ProfileResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getPhone(),
                u.getGender()
        );
    }
}
