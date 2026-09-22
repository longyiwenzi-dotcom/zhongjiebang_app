package cn.hrbzhongjiebang.cloud.membership;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CloudMembershipPlanTest {
    @Test void exposesBusinessLimits() {
        assertEquals(10, CloudMembershipPlan.WEEK.dailyLimit());
        assertEquals(30, CloudMembershipPlan.MONTH.dailyLimit());
        assertTrue(CloudMembershipPlan.QUARTER.unlimited());
        assertTrue(CloudMembershipPlan.YEAR.unlimited());
    }
}
