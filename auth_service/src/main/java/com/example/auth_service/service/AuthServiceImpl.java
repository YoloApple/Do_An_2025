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
import lombok.RequiredArgsConstructor;
import org.apache.sshd.common.config.keys.loader.openssh.kdf.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    @Value("${jwt.refresh-exp-days}") private long refreshExpDays;

    @Override
    public User signup(SignupRequest req) {
        if (userRepo.existsByUsername(req.username())) throw new RuntimeException("Username taken");
        if (userRepo.existsByEmail(req.email())) throw new RuntimeException("Email taken");
        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setPassword(encoder.encode(req.password()));
        return userRepo.save(u);
    }

    @Override
    public LoginResult login(LoginRequest req) {
        User u = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new RuntimeException("Bad credentials"));
        if (!encoder.matches(req.password(), u.getPassword())) throw new RuntimeException("Bad credentials");

        String access = jwtService.generateAccessToken(u);
        String refreshPlain = UUID.randomUUID().toString();
        String hash = BCrypt.hashpw(refreshPlain, BCrypt.gensalt());
        RefreshToken rt = new RefreshToken();
        rt.setUser(u);
        rt.setTokenHash(hash);
        rt.setExpiresAt(Instant.now().plus(refreshExpDays, ChronoUnit.DAYS));
        rtRepo.save(rt);
        return new LoginResult(u, new TokenPair(access, refreshPlain));
    }

    @Override
    public TokenPair refresh(String refreshPlain) {
        RefreshToken match = rtRepo.findAll().stream()
                .filter(rt -> !rt.isRevoked() && Instant.now().isBefore(rt.getExpiresAt())
                        && BCrypt.checkpw(refreshPlain, rt.getTokenHash()))
                .findFirst().orElseThrow(() -> new RuntimeException("Invalid refresh"));
        User u = match.getUser();
        match.setRevoked(true);
        rtRepo.save(match);

        String access = jwtService.generateAccessToken(u);
        String newRtPlain = UUID.randomUUID().toString();
        RefreshToken newRt = new RefreshToken();
        newRt.setUser(u);
        newRt.setTokenHash(BCrypt.hashpw(newRtPlain, BCrypt.gensalt()));
        newRt.setExpiresAt(Instant.now().plus(refreshExpDays, ChronoUnit.DAYS));
        rtRepo.save(newRt);

        match.setReplacedBy(newRt.getId());
        rtRepo.save(match);
        return new TokenPair(access, newRtPlain);
    }

    @Override
    public void logout(String refreshPlain) {
        rtRepo.findAll().forEach(rt -> {
            if (!rt.isRevoked() && BCrypt.checkpw(refreshPlain, rt.getTokenHash())) {
                rt.setRevoked(true);
                rtRepo.save(rt);
            }
        });
    }
}

