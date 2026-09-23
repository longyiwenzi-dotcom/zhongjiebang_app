package cn.hrbzhongjiebang.cloud.membership;

import cn.hrbzhongjiebang.cloud.contracts.HouseViewPermission;
import cn.hrbzhongjiebang.cloud.contracts.RedeemConsumeResult;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Date;
import java.time.LocalDate;

@Service
public class CloudMembershipService {
    @org.springframework.beans.factory.annotation.Value("${seata.enabled:false}")
    private boolean seataEnabled;
    private final JdbcTemplate jdbc;
    private final RedeemCodeClient redeemCodes;
    public CloudMembershipService(JdbcTemplate jdbc, RedeemCodeClient redeemCodes) { this.jdbc = jdbc; this.redeemCodes = redeemCodes; }

    @Transactional
    public HouseViewPermission consumeView(long userId, long houseId) {
        String planCode = jdbc.query("select plan_code from cloud_memberships where user_id=? and expires_at > now() for update",
                result -> result.next() ? result.getString(1) : null, userId);
        if (planCode == null) return HouseViewPermission.denied("请先开通会员");
        CloudMembershipPlan plan = CloudMembershipPlan.valueOf(planCode);
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"));
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
        if (!seataEnabled) throw new IllegalArgumentException("跨库兑换需要启用 Seata");
        RedeemConsumeResult consumed = redeemCodes.consume(new RedeemCodeClient.ConsumeRequest(userId, codeHash));
        CloudMembershipPlan plan = CloudMembershipPlan.valueOf(consumed.planCode());
        int days = switch (plan) { case WEEK -> 7; case MONTH -> 30; case QUARTER -> 90; case YEAR -> 365; };
        jdbc.update("insert into cloud_memberships(user_id, plan_code, expires_at) values (?, ?, date_add(now(), interval ? day)) " +
                        "on duplicate key update plan_code=values(plan_code), expires_at=date_add(greatest(expires_at, now()), interval ? day)",
                userId, plan.name(), days, days);
    }
}
