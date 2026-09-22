package cn.hrbzhongjiebang.user;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository {
    Optional<UserAccount> findByPhone(String phone);

    UserAccount create(String phone, String passwordHash, Instant now);

    void updatePassword(long userId, String passwordHash);

    void createSession(long userId, String tokenHash, Instant expiresAt, Instant now);

    Optional<AuthenticatedUser> findBySessionTokenHash(String tokenHash, Instant now);

    void revokeSessions(long userId);
}
