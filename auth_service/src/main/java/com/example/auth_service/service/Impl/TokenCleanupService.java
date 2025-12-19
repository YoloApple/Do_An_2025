package com.example.auth_service.service.Impl;

import com.example.auth_service.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    // 1. Chạy định kỳ mỗi ngày vào lúc 00:00:00
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        performCleanup();
    }

    // 2.Chạy NGAY LẬP TỨC khi ứng dụng khởi động xong
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void cleanupOnStartup() {
        log.info("🚀 Ứng dụng vừa khởi động. Đang kiểm tra và dọn dẹp token cũ...");
        performCleanup();
    }

    // Hàm xử lý chung để tránh lặp code
    private void performCleanup() {
        log.info("🧹 Bắt đầu dọn dẹp Refresh Token hết hạn hoặc đã bị thu hồi...");
        Instant now = Instant.now();
        try {
            refreshTokenRepository.deleteByRevokedTrueOrExpiresAtBefore(now);
            log.info("✅ Dọn dẹp token hoàn tất.");
        } catch (Exception e) {
            log.error("❌ Lỗi khi dọn dẹp token: {}", e.getMessage());
        }
    }
}
