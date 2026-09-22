package cn.hrbzhongjiebang.cloud.house;

import cn.hrbzhongjiebang.cloud.contracts.HouseViewPermission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/cloud/api/houses")
public class CloudHouseController {
    private final MembershipClient membershipClient;
    public CloudHouseController(MembershipClient membershipClient) { this.membershipClient = membershipClient; }

    @GetMapping("/{houseId}")
    public ResponseEntity<?> detail(@PathVariable long houseId, @RequestHeader("X-User-Id") long userId) {
        HouseViewPermission permission = membershipClient.consumeView(userId, houseId);
        if (!permission.allowed()) return ResponseEntity.status(403).body(permission);
        return ResponseEntity.ok(Map.of("id", houseId, "community", "轩辕小区", "price", new BigDecimal("2500.00"), "remainingViews", permission.remainingViews()));
    }
}
