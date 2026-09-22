package cn.hrbzhongjiebang;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.hrbzhongjiebang.common.BusinessException;
import cn.hrbzhongjiebang.house.CreateHouseCommand;
import cn.hrbzhongjiebang.house.House;
import cn.hrbzhongjiebang.house.HouseService;
import cn.hrbzhongjiebang.membership.MembershipService;
import cn.hrbzhongjiebang.user.AuthResult;
import cn.hrbzhongjiebang.user.AuthService;
import cn.hrbzhongjiebang.user.AuthenticatedUser;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CoreFlowIntegrationTest {
    @Autowired AuthService auth;
    @Autowired HouseService houses;
    @Autowired MembershipService memberships;
    @Autowired JdbcTemplate jdbc;

    @Test
    void tokenAuthenticatesAndPasswordIsNotStoredAsPlainText() {
        AuthResult result = auth.register("13800138000", "StrongPass123");
        AuthenticatedUser user = auth.authenticate(result.token());

        assertThat(user.phone()).isEqualTo("13800138000");
        String stored = jdbc.queryForObject("SELECT password_hash FROM users WHERE id = ?",
                String.class, user.id());
        assertThat(stored).startsWith("$2").doesNotContain("StrongPass123");
    }

    @Test
    void redeemCodeCanOnlyBeUsedOnce() throws Exception {
        AuthenticatedUser first = registered("13900139000");
        AuthenticatedUser second = registered("13700137000");
        String code = "ABCDEFGHJKLMNPQRSTUVWXYZ23";
        jdbc.update("INSERT INTO redeem_codes(code_hash, plan_code, batch_no, created_at) VALUES (?, 'WEEK', 'TEST', ?)",
                sha256(code), Instant.now());

        assertThat(memberships.redeem(first.id(), code).active()).isTrue();
        assertThatThrownBy(() -> memberships.redeem(second.id(), code))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无效或已使用");
    }

    @Test
    void onlyOwnerCanUpdatePrivateLandlordPhone() {
        AuthenticatedUser owner = registered("13600136000");
        AuthenticatedUser stranger = registered("13500135000");
        House house = houses.create(owner, new CreateHouseCommand(true, "轩辕小区", "黄河路",
                "1", "2", 8, 2, 1, 1, new BigDecimal("80.50"), null,
                new BigDecimal("2500.00"), "押一付三", true, "精装修", "南",
                List.of("https://example.test/house.jpg"), null, "测试地址", "房东", "13300133000"));

        assertThatThrownBy(() -> houses.updateLandlordPhone(stranger, house.id(), "13200132000"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只能修改自己上传的房源");
        assertThat(houses.updateLandlordPhone(owner, house.id(), "13200132000").landlordPhone())
                .isEqualTo("13200132000");
    }

    @Test
    void weeklyMemberCannotExceedDailyViewLimit() throws Exception {
        AuthenticatedUser owner = registered("13400134000");
        AuthenticatedUser viewer = registered("13100131000");
        String code = "ABCDEFGHJKLMNPQRSTUVWXYZ24";
        jdbc.update("INSERT INTO redeem_codes(code_hash, plan_code, batch_no, created_at) VALUES (?, 'WEEK', 'LIMIT', ?)",
                sha256(code), Instant.now());
        memberships.redeem(viewer.id(), code);

        for (int i = 1; i <= 10; i++) {
            House house = createHouse(owner, i);
            memberships.authorizeHouseView(viewer.id(), owner.id(), house.id());
        }
        House eleventh = createHouse(owner, 11);

        assertThatThrownBy(() -> memberships.authorizeHouseView(viewer.id(), owner.id(), eleventh.id()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("次数已用完");
    }

    private AuthenticatedUser registered(String phone) {
        return auth.authenticate(auth.register(phone, "StrongPass123").token());
    }

    private House createHouse(AuthenticatedUser owner, int number) {
        return houses.create(owner, new CreateHouseCommand(true, "测试小区", "测试街道",
                String.valueOf(number), "1", 2, 2, 1, 1, new BigDecimal("60.00"), null,
                new BigDecimal("2000.00"), "押一付三", true, "精装修", "南",
                List.of(), null, null, null, null));
    }

    private String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
