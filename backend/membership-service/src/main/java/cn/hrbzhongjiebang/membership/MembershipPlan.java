package cn.hrbzhongjiebang.membership;

import cn.hrbzhongjiebang.common.BusinessException;
import java.time.Period;

public enum MembershipPlan {
    WEEK("周卡", Period.ofDays(7), 10),
    MONTH("月卡", Period.ofMonths(1), 30),
    QUARTER("季卡", Period.ofMonths(3), Integer.MAX_VALUE),
    YEAR("年卡", Period.ofYears(1), Integer.MAX_VALUE);

    private final String displayName;
    private final Period duration;
    private final int dailyViewLimit;

    MembershipPlan(String displayName, Period duration, int dailyViewLimit) {
        this.displayName = displayName;
        this.duration = duration;
        this.dailyViewLimit = dailyViewLimit;
    }

    public String displayName() { return displayName; }
    public Period duration() { return duration; }
    public int dailyViewLimit() { return dailyViewLimit; }

    public static MembershipPlan parse(String value) {
        try {
            return valueOf(value);
        } catch (RuntimeException e) {
            throw new BusinessException("INVALID_PLAN", "会员套餐不存在");
        }
    }
}
