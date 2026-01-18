package com.greenwich.flowerplus.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupJob {

    private final JdbcTemplate jdbcTemplate;

    // Chạy lúc 1h sáng mỗi ngày
    @Scheduled(cron = "0 0 1 * * *")
    @SchedulerLock(name = "TokenCleanupJob_cleanExpiredTokens",
            lockAtLeastFor = "1m", lockAtMostFor = "10m")
    public void cleanExpiredTokens() {
        log.info("🧹 Bắt đầu dọn dẹp Refresh Token hết hạn/revoked...");

        // Logic: Xóa token thỏa mãn:
        // 1. Đã Revoked HOẶC Đã Hết hạn
        // 2. VÀ thời điểm tạo/hết hạn đã trôi qua 7 ngày (Retention Period)
        String sql = """
            DELETE FROM refresh_tokens\s
            WHERE (revoked = true OR expiry_date < NOW())
            AND expiry_date < NOW() - INTERVAL '7 days'
            AND id IN (
                SELECT id FROM refresh_tokens\s
                WHERE (revoked = true OR expiry_date < NOW())
                AND expiry_date < NOW() - INTERVAL '7 days'
                LIMIT 1000
            )
       \s""";

        int totalDeleted = 0;
        int deletedCount;

        do {
            deletedCount = jdbcTemplate.update(sql);
            totalDeleted += deletedCount;
            // Nghỉ nhẹ 100ms
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        } while (deletedCount > 0);

        log.info("✅ Đã xóa vĩnh viễn {} refresh token cũ.", totalDeleted);
    }
}
