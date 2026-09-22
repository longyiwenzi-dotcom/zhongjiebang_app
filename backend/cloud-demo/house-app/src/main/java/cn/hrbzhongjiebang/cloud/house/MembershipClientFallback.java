package cn.hrbzhongjiebang.cloud.house;

import cn.hrbzhongjiebang.cloud.contracts.HouseViewPermission;
import org.springframework.stereotype.Component;

@Component
public class MembershipClientFallback implements MembershipClient {
    @Override
    public HouseViewPermission consumeView(long userId, long houseId) {
        return HouseViewPermission.denied("会员服务暂不可用，请稍后重试");
    }
}
