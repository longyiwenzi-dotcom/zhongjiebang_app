package cn.hrbzhongjiebang.security;

import cn.hrbzhongjiebang.common.BusinessException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ConditionalOnProperty(name = "app.redis-rate-limit.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryAttemptLimiter implements AttemptLimiter {
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public void check(String action, String subject, int limit, long windowSeconds) {
        long now = Instant.now().getEpochSecond();
        Window window = windows.compute(action + ':' + subject, (key, old) ->
                old == null || old.expiresAt <= now ? new Window(now + windowSeconds) : old);
        if (window.count.incrementAndGet() > limit) throw new BusinessException("RATE_LIMITED", "操作过于频繁，请稍后再试");
    }

    private static final class Window {
        private final long expiresAt;
        private final AtomicInteger count = new AtomicInteger();
        private Window(long expiresAt) { this.expiresAt = expiresAt; }
    }
}
