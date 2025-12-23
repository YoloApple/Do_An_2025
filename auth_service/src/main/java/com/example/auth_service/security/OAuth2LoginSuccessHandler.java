package com.example.auth_service.security;

import com.example.auth_service.dto.LoginResult;
import com.example.auth_service.dto.TokenPair;
import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.util.HashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    // Lưu tạm token vào memory (Production nên dùng Redis)
    private static final ConcurrentHashMap<String, LoginResult> tokenCache = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${jwt.refresh-exp-days:7}")
    private long refreshExpDays;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // 1. Tìm hoặc tạo User
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    String baseUsername = email.split("@")[0];
                    String username = baseUsername;
                    if (userRepository.existsByUsername(username)) {
                        username = baseUsername + "_" + UUID.randomUUID().toString().substring(0, 4);
                    }
                    newUser.setUsername(username);
                    newUser.setEnabled(true);
                    newUser.setPassword(null);
                    return userRepository.save(newUser);
                });

        // 2. Tạo Token Pair
        String accessToken = jwtService.generateAccessToken(user);
        String refreshPlain = UUID.randomUUID().toString();
        String refreshHash = HashUtil.sha256(refreshPlain);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(refreshHash);
        rt.setExpiresAt(Instant.now().plus(refreshExpDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(rt);

        // 3. Tạo code ngắn hạn và lưu vào cache
        String code = UUID.randomUUID().toString();
        LoginResult result = new LoginResult(user, new TokenPair(accessToken, refreshPlain));
        tokenCache.put(code, result);
        
        // Xóa code sau 60s
        new Thread(() -> {
            try {
                Thread.sleep(60000);
                tokenCache.remove(code);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        // 4. Redirect về FE với code (không phải token)
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("code", code)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    public static LoginResult getByCode(String code) {
        return tokenCache.get(code);
    }

    public static void removeCode(String code) {
        tokenCache.remove(code);
    }
}
