package cn.hrbzhongjiebang.security;

import cn.hrbzhongjiebang.common.BusinessException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.redis-rate-limit.enabled", havingValue = "true")
public class RedisAttemptLimiter implements AttemptLimiter {
    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>(
            "local n=redis.call('INCR',KEYS[1]); if n==1 then redis.call('EXPIRE',KEYS[1],ARGV[1]) end; return n", Long.class);
    private final StringRedisTemplate redis;

    public RedisAttemptLimiter(StringRedisTemplate redis) { this.redis = redis; }

    @Override
    public void check(String action, String subject, int limit, long windowSeconds) {
        Long attempts = redis.execute(SCRIPT, List.of("zjb:limit:" + action + ':' + subject), Long.toString(windowSeconds));
        if (attempts == null || attempts > limit) throw new BusinessException("RATE_LIMITED", "操作过于频繁，请稍后再试");
    }
}
