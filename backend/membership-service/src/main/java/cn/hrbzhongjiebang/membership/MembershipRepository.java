package cn.hrbzhongjiebang.membership;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

public interface MembershipRepository {
    Optional<MembershipRecord> findActive(long userId, Instant now);

    RedeemCodeRecord lockUnusedCode(String codeHash);

    void markCodeUsed(long codeId, long userId, Instant now);

    void saveMembership(long userId, MembershipPlan plan, Instant startsAt, Instant expiresAt);

    boolean recordFirstView(long userId, long houseId, LocalDate date, Instant now);

    boolean consumeViewSlot(long userId, LocalDate date, int limit, Instant now);

    int countViews(long userId, LocalDate date);

    record MembershipRecord(MembershipPlan plan, Instant startsAt, Instant expiresAt) {}
    record RedeemCodeRecord(long id, MembershipPlan plan) {}
}
