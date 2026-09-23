package cn.hrbzhongjiebang.user;

import cn.hrbzhongjiebang.common.BusinessException;
import cn.hrbzhongjiebang.common.PhoneNumber;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final Duration SESSION_LIFETIME = Duration.ofDays(30);
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, Clock clock) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public AuthResult register(String rawPhone, String password) {
        String phone = new PhoneNumber(rawPhone).value();
        if (users.findByPhone(phone).isPresent()) {
            throw new BusinessException("PHONE_REGISTERED", "该手机号已经注册");
        }
        Instant now = clock.instant();
        validatePassword(password);
        UserAccount user = users.create(phone, passwordEncoder.encode(password), now);
        return issueSession(user, now);
    }

    @Transactional
    public AuthResult loginByPassword(String rawPhone, String password) {
        String phone = new PhoneNumber(rawPhone).value();
        validatePassword(password);
        UserAccount user = users.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "手机号或密码错误"));
        if (!user.hasPassword() || password == null || !passwordEncoder.matches(password, user.passwordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS", "手机号或密码错误");
        }
        return issueSession(user, clock.instant());
    }

    public AuthenticatedUser authenticate(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("UNAUTHORIZED", "请先登录");
        }
        return users.findBySessionTokenHash(hashToken(token), clock.instant())
                .orElseThrow(() -> new BusinessException("UNAUTHORIZED", "登录状态已失效"));
    }

    @Transactional
    public void changePassword(long userId, String newPassword) {
        validatePassword(newPassword);
        users.updatePassword(userId, passwordEncoder.encode(newPassword));
        users.revokeSessions(userId);
    }

    private AuthResult issueSession(UserAccount user, Instant now) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        users.createSession(user.id(), hashToken(token), now.plus(SESSION_LIFETIME), now);
        return new AuthResult(token, user.phone(), user.hasPassword());
    }

    private String encodeOptionalPassword(String password) {
        if (password == null || password.isBlank()) {
            return "";
        }
        validatePassword(password);
        return passwordEncoder.encode(password);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("INVALID_PASSWORD", "密码长度应为8至72位");
        }
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
