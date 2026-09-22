package cn.hrbzhongjiebang.cloud.house;

import cn.hrbzhongjiebang.cloud.contracts.HouseViewPermission;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "zjb-membership", fallback = MembershipClientFallback.class)
public interface MembershipClient {
    @PostMapping("/internal/memberships/{userId}/house-views/{houseId}")
    HouseViewPermission consumeView(@PathVariable long userId, @PathVariable long houseId);
}
