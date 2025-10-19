package com.example.auth_service.service;

import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.LoginResult;
import com.example.auth_service.dto.SignupRequest;
import com.example.auth_service.dto.TokenPair;
import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.JwtService;
import com.example.auth_service.service.Impl.AuthService;
import com.example.auth_service.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepo;
    private final RefreshTokenRepository rtRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @Value("${jwt.refresh-exp-days:7}") private long refreshExpDays;

    @Override
    @Transactional
    public LoginResult signup(SignupRequest req) {
        if (userRepo.existsByUsername(req.username())) throw new RuntimeException("Username taken");
        if (userRepo.existsByEmail(req.email())) throw new RuntimeException("Email taken");
        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setPassword(encoder.encode(req.password()));
        User savedUser = userRepo.save(u);

        // Sau khi lưu user, tạo token và trả về kết quả đăng nhập
        return generateTokensAndLoginResult(savedUser);
    }

    @Override
    @Transactional
    public LoginResult login(LoginRequest req) {
        User u = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new RuntimeException("Bad credentials"));
        if (!encoder.matches(req.password(), u.getPassword())) throw new RuntimeException("Bad credentials");

        return generateTokensAndLoginResult(u);
    }

    // Phương thức để tạo token, được sử dụng bởi cả login và signup
    private LoginResult generateTokensAndLoginResult(User user) {
        String access = jwtService.generateAccessToken(user);
        String refreshPlain = UUID.randomUUID().toString();
        String hash = HashUtil.sha256(refreshPlain);
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hash);
        rt.setExpiresAt(Instant.now().plus(refreshExpDays, ChronoUnit.DAYS));
        rtRepo.save(rt);
        return new LoginResult(user, new TokenPair(access, refreshPlain));
    }

    @Override
    @Transactional
    public TokenPair refresh(String refreshPlain) {
        String hash = HashUtil.sha256(refreshPlain);
        RefreshToken match = rtRepo.findByTokenHash(hash)
                .filter(rt -> !rt.isRevoked() && Instant.now().isBefore(rt.getExpiresAt()))
                .orElseThrow(() -> new RuntimeException("Invalid refresh"));

        User u = match.getUser();
        match.setRevoked(true);

        String access = jwtService.generateAccessToken(u);
        String newRtPlain = UUID.randomUUID().toString();
        String newRtHash = HashUtil.sha256(newRtPlain);
        RefreshToken newRt = new RefreshToken();
        newRt.setUser(u);
        newRt.setTokenHash(newRtHash);
        newRt.setExpiresAt(Instant.now().plus(refreshExpDays, ChronoUnit.DAYS));
        rtRepo.save(newRt);

        match.setReplacedBy(newRt.getId());
        rtRepo.save(match);
        return new TokenPair(access, newRtPlain);
    }

    @Override
    @Transactional
    public void logout(String refreshPlain) {
        String hash = HashUtil.sha256(refreshPlain);
        rtRepo.findByTokenHash(hash).ifPresent(rt -> {
            if (!rt.isRevoked()) {
                rt.setRevoked(true);
                rtRepo.save(rt);
            }
        });
    }
}

