package cn.hrbzhongjiebang.security;

import cn.hrbzhongjiebang.common.BusinessException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryAttemptLimiterTest {
    @Test
    void blocksRequestsAfterConfiguredLimit() {
        InMemoryAttemptLimiter limiter = new InMemoryAttemptLimiter();
        assertDoesNotThrow(() -> limiter.check("login", "client", 2, 60));
        assertDoesNotThrow(() -> limiter.check("login", "client", 2, 60));
        assertThrows(BusinessException.class, () -> limiter.check("login", "client", 2, 60));
    }
}
