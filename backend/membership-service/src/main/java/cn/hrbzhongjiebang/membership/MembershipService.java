package cn.hrbzhongjiebang.membership;

import cn.hrbzhongjiebang.common.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private final MembershipRepository memberships;
    private final Clock clock;

    public MembershipService(MembershipRepository memberships, Clock clock) {
        this.memberships = memberships;
        this.clock = clock;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MembershipStatus redeem(long userId, String rawCode) {
        String code = normalizeCode(rawCode);
        Instant now = clock.instant();
        MembershipRepository.RedeemCodeRecord redeemCode = memberships.lockUnusedCode(hash(code));
        MembershipPlan plan = redeemCode.plan();
        Instant startsAt = memberships.findActive(userId, now)
                .map(MembershipRepository.MembershipRecord::expiresAt)
                .orElse(now);
        Instant expiresAt = startsAt.atZone(BUSINESS_ZONE).plus(plan.duration()).toInstant();
        memberships.markCodeUsed(redeemCode.id(), userId, now);
        memberships.saveMembership(userId, plan, startsAt, expiresAt);
        return status(userId);
    }

    public MembershipStatus status(long userId) {
        Instant now = clock.instant();
        return memberships.findActive(userId, now)
                .map(record -> {
                    int used = memberships.countViews(userId, LocalDate.now(clock.withZone(BUSINESS_ZONE)));
                    int limit = record.plan().dailyViewLimit();
                    int remaining = limit == Integer.MAX_VALUE ? -1 : Math.max(0, limit - used);
                    return new MembershipStatus(true, record.plan().name(), record.plan().displayName(),
                            record.expiresAt(), remaining);
                })
                .orElseGet(MembershipStatus::inactive);
    }

    @Transactional
    public void authorizeHouseView(long userId, long ownerId, long houseId) {
        if (userId == ownerId) return;
        Instant now = clock.instant();
        MembershipRepository.MembershipRecord active = memberships.findActive(userId, now)
                .orElseThrow(() -> new BusinessException("MEMBERSHIP_REQUIRED", "开通会员后可查看房源详情"));
        if (active.plan().dailyViewLimit() == Integer.MAX_VALUE) return;
        LocalDate today = LocalDate.now(clock.withZone(BUSINESS_ZONE));
        if (!memberships.recordFirstView(userId, houseId, today, now)) return;
        if (!memberships.consumeViewSlot(userId, today, active.plan().dailyViewLimit(), now)) {
            throw new BusinessException("DAILY_LIMIT_REACHED", "今日房源查看次数已用完");
        }
    }

    private String normalizeCode(String code) {
        String normalized = code == null ? "" : code.replace("-", "").trim().toUpperCase();
        if (!normalized.matches("^[A-Z2-9]{26}$")) {
            throw new BusinessException("INVALID_REDEEM_CODE", "兑换码格式不正确");
        }
        return normalized;
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
