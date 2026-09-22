package cn.hrbzhongjiebang.cloud.membership;

import cn.hrbzhongjiebang.cloud.contracts.HouseViewPermission;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CloudMembershipService {
    private final JdbcTemplate jdbc;
    public CloudMembershipService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public HouseViewPermission consumeView(long userId, long houseId) {
        Integer active = jdbc.queryForObject("select count(*) from cloud_memberships where user_id=? and expires_at > now()", Integer.class, userId);
        return active != null && active > 0
                ? new HouseViewPermission(true, "ok", 9)
                : HouseViewPermission.denied("请先开通会员");
    }

    @GlobalTransactional(name = "redeem-membership", rollbackFor = Exception.class)
    public void redeem(long userId, String codeHash) {
        int consumed = jdbc.update("update cloud_redeem_codes set used_by=?, used_at=now() where code_hash=? and used_at is null", userId, codeHash);
        if (consumed != 1) throw new IllegalArgumentException("兑换码无效或已使用");
        jdbc.update("insert into cloud_memberships(user_id, plan_code, expires_at) values (?, 'WEEK', date_add(now(), interval 7 day)) on duplicate key update plan_code='WEEK', expires_at=date_add(greatest(expires_at, now()), interval 7 day)", userId);
    }
}
