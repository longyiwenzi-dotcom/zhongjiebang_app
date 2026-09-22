package cn.hrbzhongjiebang.membership;

import java.time.Instant;

public record MembershipStatus(boolean active, String plan, String planName, Instant expiresAt, int remainingViews) {
    public static MembershipStatus inactive() {
        return new MembershipStatus(false, null, null, null, 0);
    }
}
