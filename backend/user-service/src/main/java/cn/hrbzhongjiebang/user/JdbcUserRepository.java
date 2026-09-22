package cn.hrbzhongjiebang.user;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserRepository implements UserRepository {
    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<UserAccount> findByPhone(String phone) {
        List<UserAccount> users = jdbc.query("""
                SELECT id, phone, password_hash, role, created_at
                FROM users WHERE phone = ?
                """, (rs, row) -> new UserAccount(
                rs.getLong("id"), rs.getString("phone"), rs.getString("password_hash"),
                rs.getString("role"), rs.getTimestamp("created_at").toInstant()), phone);
        return users.stream().findFirst();
    }

    @Override
    public UserAccount create(String phone, String passwordHash, Instant now) {
        jdbc.update("INSERT INTO users(phone, password_hash, role, created_at) VALUES (?, ?, 'USER', ?)",
                phone, passwordHash, Timestamp.from(now));
        return findByPhone(phone).orElseThrow();
    }

    @Override
    public void updatePassword(long userId, String passwordHash) {
        jdbc.update("UPDATE users SET password_hash = ? WHERE id = ?", passwordHash, userId);
    }

    @Override
    public void createSession(long userId, String tokenHash, Instant expiresAt, Instant now) {
        jdbc.update("INSERT INTO user_sessions(user_id, token_hash, expires_at, created_at) VALUES (?, ?, ?, ?)",
                userId, tokenHash, Timestamp.from(expiresAt), Timestamp.from(now));
    }

    @Override
    public Optional<AuthenticatedUser> findBySessionTokenHash(String tokenHash, Instant now) {
        List<AuthenticatedUser> users = jdbc.query("""
                SELECT u.id, u.phone, u.role
                FROM user_sessions s JOIN users u ON u.id = s.user_id
                WHERE s.token_hash = ? AND s.expires_at > ?
                """, (rs, row) -> new AuthenticatedUser(
                rs.getLong("id"), rs.getString("phone"), rs.getString("role")),
                tokenHash, Timestamp.from(now));
        return users.stream().findFirst();
    }

    @Override
    public void revokeSessions(long userId) {
        jdbc.update("DELETE FROM user_sessions WHERE user_id = ?", userId);
    }
}
