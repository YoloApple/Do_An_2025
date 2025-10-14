package com.example.auth_service.controller;

import com.example.auth_service.dto.*;
import com.example.auth_service.entity.User;
import com.example.auth_service.service.AuthServiceImpl;
import com.example.auth_service.service.Impl.AuthService;
import com.example.auth_service.util.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @Value("${cookie.domain}") private String cookieDomain;
    @Value("${cookie.secure}") private boolean cookieSecure;
    @Value("${cookie.samesite}") private String cookieSameSite;
    @Value("${jwt.access-exp-minutes}") private int accessExpMinutes;
    @Value("${jwt.refresh-exp-days}") private int refreshExpDays;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest req) {
        User user = authService.signup(req);
        AuthResponse response = new AuthResponse(user.getId(), user.getUsername(), user.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Signup successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        LoginResult result = authService.login(req);
        User user = result.user();
        TokenPair pair = result.tokenPair();

        // set cookie
        var accessCookie = CookieUtil.httpOnly(
                "ACCESS_TOKEN", pair.accessToken(), accessExpMinutes * 60,
                cookieDomain, cookieSecure, cookieSameSite);

        var refreshCookie = CookieUtil.httpOnly(
                "REFRESH_TOKEN", pair.refreshToken(), refreshExpDays * 24 * 60 * 60,
                cookieDomain, cookieSecure, cookieSameSite);

        AuthResponse authResponse = new AuthResponse(user.getId(), user.getUsername(), user.getEmail());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success(authResponse, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(@CookieValue("REFRESH_TOKEN") String rt) {
        TokenPair pair = authService.refresh(rt);

        var accessCookie = CookieUtil.httpOnly(
                "ACCESS_TOKEN", pair.accessToken(), accessExpMinutes * 60,
                cookieDomain, cookieSecure, cookieSameSite);

        var refreshCookie = CookieUtil.httpOnly(
                "REFRESH_TOKEN", pair.refreshToken(), refreshExpDays * 24 * 60 * 60,
                cookieDomain, cookieSecure, cookieSameSite);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success("Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue("REFRESH_TOKEN") String rt) {
        authService.logout(rt);

        var clearA = CookieUtil.httpOnly("ACCESS_TOKEN", "", 0, cookieDomain, cookieSecure, cookieSameSite);
        var clearR = CookieUtil.httpOnly("REFRESH_TOKEN", "", 0, cookieDomain, cookieSecure, cookieSameSite);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearA.toString())
                .header(HttpHeaders.SET_COOKIE, clearR.toString())
                .build();
    }
}

