package cn.hrbzhongjiebang.cloud.membership;

import cn.hrbzhongjiebang.cloud.contracts.HouseViewPermission;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Date;
import java.time.LocalDate;

@Service
public class CloudMembershipService {
    private final JdbcTemplate jdbc;
    public CloudMembershipService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public HouseViewPermission consumeView(long userId, long houseId) {
        String planCode = jdbc.query("select plan_code from cloud_memberships where user_id=? and expires_at > now() for update",
                result -> result.next() ? result.getString(1) : null, userId);
        if (planCode == null) return HouseViewPermission.denied("请先开通会员");
        CloudMembershipPlan plan = CloudMembershipPlan.valueOf(planCode);
        LocalDate today = LocalDate.now();
        Integer viewed = jdbc.queryForObject("select count(*) from cloud_house_view_events where user_id=? and view_date=?",
                Integer.class, userId, Date.valueOf(today));
        Integer alreadyViewed = jdbc.queryForObject("select count(*) from cloud_house_view_events where user_id=? and house_id=? and view_date=?",
                Integer.class, userId, houseId, Date.valueOf(today));
        int used = viewed == null ? 0 : viewed;
        if ((alreadyViewed == null || alreadyViewed == 0) && !plan.unlimited() && used >= plan.dailyLimit()) {
            return HouseViewPermission.denied("今日查看额度已用完");
        }
        if (alreadyViewed == null || alreadyViewed == 0) {
            jdbc.update("insert ignore into cloud_house_view_events(user_id, house_id, view_date) values (?, ?, ?)",
                    userId, houseId, Date.valueOf(today));
            used++;
        }
        int remaining = plan.unlimited() ? -1 : Math.max(0, plan.dailyLimit() - used);
        return new HouseViewPermission(true, "ok", remaining);
    }

    @GlobalTransactional(name = "redeem-membership", rollbackFor = Exception.class)
    @Transactional
    public void redeem(long userId, String codeHash) {
        RedeemCode code = jdbc.query("select id, plan_code from cloud_redeem_codes where code_hash=? and used_at is null for update",
                result -> result.next() ? new RedeemCode(result.getLong(1), CloudMembershipPlan.valueOf(result.getString(2))) : null, codeHash);
        if (code == null) throw new IllegalArgumentException("兑换码无效或已使用");
        int consumed = jdbc.update("update cloud_redeem_codes set used_by=?, used_at=now() where id=? and used_at is null", userId, code.id());
        if (consumed != 1) throw new IllegalArgumentException("兑换码无效或已使用");
        int days = switch (code.plan()) { case WEEK -> 7; case MONTH -> 30; case QUARTER -> 90; case YEAR -> 365; };
        jdbc.update("insert into cloud_memberships(user_id, plan_code, expires_at) values (?, ?, date_add(now(), interval ? day)) " +
                        "on duplicate key update plan_code=values(plan_code), expires_at=date_add(greatest(expires_at, now()), interval ? day)",
                userId, code.plan().name(), days, days);
    }

    private record RedeemCode(long id, CloudMembershipPlan plan) { }
}
