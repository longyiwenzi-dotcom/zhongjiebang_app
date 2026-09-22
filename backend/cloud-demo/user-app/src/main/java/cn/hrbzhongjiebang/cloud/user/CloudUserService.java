package cn.hrbzhongjiebang.cloud.user;

import cn.hrbzhongjiebang.cloud.contracts.AuthSession;
import cn.hrbzhongjiebang.cloud.contracts.UserSummary;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CloudUserService {
    private static final Duration SESSION_TTL = Duration.ofHours(12);
    private final JdbcTemplate jdbc;
    private final StringRedisTemplate redis;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public CloudUserService(JdbcTemplate jdbc, StringRedisTemplate redis) { this.jdbc = jdbc; this.redis = redis; }

    @Transactional
    public AuthSession register(String phone, String password) {
        validate(phone, password);
        try { jdbc.update("insert into cloud_users(phone, password_hash, active) values (?, ?, true)", phone, encoder.encode(password)); }
        catch (DuplicateKeyException error) { throw new IllegalArgumentException("手机号已注册"); }
        return login(phone, password);
    }

    public AuthSession login(String phone, String password) {
        validate(phone, password);
        Account account = jdbc.query("select id, password_hash from cloud_users where phone=? and active=true",
                result -> result.next() ? new Account(result.getLong(1), result.getString(2)) : null, phone);
        if (account == null || !encoder.matches(password, account.passwordHash())) throw new IllegalArgumentException("手机号或密码错误");
        String token = UUID.randomUUID() + "." + UUID.randomUUID();
        redis.opsForValue().set("zjb:session:" + sha256(token), Long.toString(account.id()), SESSION_TTL);
        return new AuthSession(token, account.id(), SESSION_TTL.toSeconds());
    }

    public UserSummary find(long id) {
        return jdbc.query("select phone, active from cloud_users where id=?", result -> {
            if (!result.next()) throw new IllegalArgumentException("用户不存在");
            String phone = result.getString(1);
            return new UserSummary(id, phone.substring(0, 3) + "****" + phone.substring(7), result.getBoolean(2));
        }, id);
    }

    private static void validate(String phone, String password) {
        if (phone == null || !phone.matches("1\\d{10}")) throw new IllegalArgumentException("手机号格式不正确");
        if (password == null || password.length() < 8 || password.length() > 72) throw new IllegalArgumentException("密码长度必须为8到72位");
    }
    private static String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }
    private record Account(long id, String passwordHash) { }
}
