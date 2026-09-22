package cn.hrbzhongjiebang.cloud.membership;

public enum CloudMembershipPlan {
    WEEK(10), MONTH(30), QUARTER(Integer.MAX_VALUE), YEAR(Integer.MAX_VALUE);
    private final int dailyLimit;
    CloudMembershipPlan(int dailyLimit) { this.dailyLimit = dailyLimit; }
    public int dailyLimit() { return dailyLimit; }
    public boolean unlimited() { return dailyLimit == Integer.MAX_VALUE; }
}
