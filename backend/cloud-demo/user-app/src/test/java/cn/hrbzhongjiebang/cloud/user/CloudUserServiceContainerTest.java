package cn.hrbzhongjiebang.cloud.user;

import cn.hrbzhongjiebang.cloud.contracts.AuthSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "management.tracing.enabled=false",
        "app.internal-token=test-internal-012345678901234567890"
})
class CloudUserServiceContainerTest {
    @Container static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("zhongjiebang_cloud").withUsername("test").withPassword("test");
    @Container static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired CloudUserService service;
    @Autowired JdbcTemplate jdbc;
    @Autowired StringRedisTemplate redis;

    @BeforeEach
    void schema() {
        jdbc.execute("drop table if exists cloud_users");
        jdbc.execute("create table cloud_users(id bigint primary key auto_increment, phone char(11) not null unique, password_hash varchar(100) not null, active boolean not null, created_at datetime(3) default current_timestamp(3))");
        redis.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void registerAndLoginUseBcryptAndHashedRedisSession() {
        AuthSession registration = service.register("18100000001", "DemoPass123");
        String passwordHash = jdbc.queryForObject("select password_hash from cloud_users where id=?", String.class, registration.userId());
        assertNotEquals("DemoPass123", passwordHash);
        assertTrue(passwordHash.startsWith("$2"));
        assertNull(redis.opsForValue().get("zjb:session:" + registration.accessToken()));
        assertEquals(Long.toString(registration.userId()), redis.opsForValue().get("zjb:session:" + sha256(registration.accessToken())));

        AuthSession login = service.login("18100000001", "DemoPass123");
        assertNotEquals(registration.accessToken(), login.accessToken());
        assertThrows(IllegalArgumentException.class, () -> service.login("18100000001", "wrong-pass"));
    }

    private static String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }
}
