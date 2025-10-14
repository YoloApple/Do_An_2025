package com.example.auth_service.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtil {
    public static ResponseCookie httpOnly(String name, String value, int maxAgeSeconds,
                                          String domain, boolean secure, String sameSite) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds));
        if (domain != null && !domain.isBlank()) b.domain(domain);
        if (sameSite != null && !sameSite.isBlank()) b.sameSite(sameSite);
        return b.build();
    }
}

