package cn.hrbzhongjiebang.user;

import java.time.Instant;

public record UserAccount(long id, String phone, String passwordHash, String role, Instant createdAt) {
    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isBlank();
    }
}
