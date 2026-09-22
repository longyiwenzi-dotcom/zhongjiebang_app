package cn.hrbzhongjiebang.cloud.redeem;

import cn.hrbzhongjiebang.cloud.contracts.RedeemConsumeResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RedeemCodeService {
    private final JdbcTemplate jdbc;
    public RedeemCodeService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public RedeemConsumeResult consume(long userId, String codeHash) {
        RedeemConsumeResult code = jdbc.query("select id, plan_code from redeem_codes where code_hash=? and used_at is null for update",
                result -> result.next() ? new RedeemConsumeResult(result.getLong(1), result.getString(2)) : null, codeHash);
        if (code == null) throw new IllegalArgumentException("兑换码无效或已使用");
        int updated = jdbc.update("update redeem_codes set used_by=?, used_at=now() where id=? and used_at is null", userId, code.codeId());
        if (updated != 1) throw new IllegalStateException("兑换码并发消费失败");
        return code;
    }
}
