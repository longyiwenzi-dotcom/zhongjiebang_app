package cn.hrbzhongjiebang.membership;

import cn.hrbzhongjiebang.common.BusinessException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcMembershipRepository implements MembershipRepository {
    private final JdbcTemplate jdbc;

    public JdbcMembershipRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<MembershipRecord> findActive(long userId, Instant now) {
        List<MembershipRecord> result = jdbc.query("""
                SELECT plan_code, starts_at, expires_at FROM memberships
                WHERE user_id = ? AND expires_at > ? ORDER BY expires_at DESC LIMIT 1
                """, (rs, row) -> new MembershipRecord(MembershipPlan.parse(rs.getString("plan_code")),
                rs.getTimestamp("starts_at").toInstant(), rs.getTimestamp("expires_at").toInstant()),
                userId, Timestamp.from(now));
        return result.stream().findFirst();
    }

    @Override
    public RedeemCodeRecord lockUnusedCode(String codeHash) {
        List<RedeemCodeRecord> result = jdbc.query("""
                SELECT id, plan_code FROM redeem_codes
                WHERE code_hash = ? AND used_at IS NULL FOR UPDATE
                """, (rs, row) -> new RedeemCodeRecord(rs.getLong("id"),
                MembershipPlan.parse(rs.getString("plan_code"))), codeHash);
        return result.stream().findFirst()
                .orElseThrow(() -> new BusinessException("INVALID_REDEEM_CODE", "兑换码无效或已使用"));
    }

    @Override
    public void markCodeUsed(long codeId, long userId, Instant now) {
        int updated = jdbc.update("""
                UPDATE redeem_codes SET used_by = ?, used_at = ?
                WHERE id = ? AND used_at IS NULL
                """, userId, Timestamp.from(now), codeId);
        if (updated != 1) {
            throw new BusinessException("REDEEM_CODE_USED", "兑换码已使用");
        }
    }

    @Override
    public void saveMembership(long userId, MembershipPlan plan, Instant startsAt, Instant expiresAt) {
        jdbc.update("INSERT INTO memberships(user_id, plan_code, starts_at, expires_at) VALUES (?, ?, ?, ?)",
                userId, plan.name(), Timestamp.from(startsAt), Timestamp.from(expiresAt));
    }

    @Override
    public boolean recordFirstView(long userId, long houseId, LocalDate date, Instant now) {
        try {
            jdbc.update("INSERT INTO house_view_records(user_id, house_id, view_date, created_at) VALUES (?, ?, ?, ?)",
                    userId, houseId, date, Timestamp.from(now));
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    @Override
    public boolean consumeViewSlot(long userId, LocalDate date, int limit, Instant now) {
        try {
            jdbc.update("""
                    INSERT INTO daily_view_counters(user_id, view_date, view_count, updated_at)
                    VALUES (?, ?, 0, ?)
                    """, userId, date, Timestamp.from(now));
        } catch (DuplicateKeyException ignored) {
            // The existing row is locked below before checking and incrementing.
        }
        Integer used = jdbc.queryForObject("""
                SELECT view_count FROM daily_view_counters
                WHERE user_id = ? AND view_date = ? FOR UPDATE
                """, Integer.class, userId, date);
        if (used == null || used >= limit) return false;
        jdbc.update("""
                UPDATE daily_view_counters SET view_count = view_count + 1, updated_at = ?
                WHERE user_id = ? AND view_date = ?
                """, Timestamp.from(now), userId, date);
        return true;
    }

    @Override
    public int countViews(long userId, LocalDate date) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM house_view_records WHERE user_id = ? AND view_date = ?",
                Integer.class, userId, date);
        return count == null ? 0 : count;
    }
}
