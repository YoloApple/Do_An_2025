package com.example.auth_service.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

@Component
@Slf4j
public class CookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180; // 3 phút

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        log.info("🔍 Loading authorization request from cookie...");
        log.info("   Request URI: {}", request.getRequestURI());
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            log.info("   Available cookies:");
            for (Cookie cookie : cookies) {
                log.info("     - {}: {} bytes", cookie.getName(), cookie.getValue().length());
            }
        } else {
            log.warn("   ❌ No cookies in request!");
        }
        
        OAuth2AuthorizationRequest authRequest = getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(this::deserialize)
                .orElse(null);
        
        if (authRequest != null) {
            log.info("   ✅ Found authorization request in cookie");
            log.info("   State from cookie: {}", authRequest.getState());
        } else {
            log.warn("   ❌ No authorization request found in cookie");
        }
        
        return authRequest;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, 
                                          HttpServletRequest request, 
                                          HttpServletResponse response) {
        if (authorizationRequest == null) {
            log.info("🗑️ Deleting authorization request cookie");
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            return;
        }

        log.info("💾 Saving authorization request to cookie");
        log.info("   State: {}", authorizationRequest.getState());
        log.info("   Request URI: {}", request.getRequestURI());
        
        String value = serialize(authorizationRequest);
        log.info("   Cookie size: {} bytes", value.length());
        
        // ✅ FIX: Sử dụng ResponseCookie
        ResponseCookie cookie = ResponseCookie
            .from(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, value)
            .path("/")
            .httpOnly(true)
            .secure(true)  
            .sameSite("None") 
            .maxAge(COOKIE_EXPIRE_SECONDS)
            .build();
        
        response.addHeader("Set-Cookie", cookie.toString());
        log.info("   ✅ Cookie saved with SameSite=None");
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, 
                                                                  HttpServletResponse response) {
        log.info("🗑️ Removing authorization request");
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        if (authorizationRequest != null) {
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            log.info("   ✅ Authorization request removed");
        }
        return authorizationRequest;
    }

    private java.util.Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return java.util.Optional.of(cookie);
                }
            }
        }
        return java.util.Optional.empty();
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie
            .from(name, "")
            .path("/")
            .httpOnly(true)
            .secure(false)
            .sameSite("None")
            .maxAge(0)
            .build();
        
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        return Base64.getUrlEncoder().encodeToString(
            SerializationUtils.serialize(authorizationRequest)
        );
    }

    private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
        try {
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(cookie.getValue())
            );
        } catch (Exception e) {
            log.error("❌ Failed to deserialize authorization request: {}", e.getMessage());
            return null;
        }
    }
}
