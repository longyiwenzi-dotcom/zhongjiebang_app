package cn.hrbzhongjiebang.cloud.membership;

import cn.hrbzhongjiebang.cloud.contracts.HouseViewPermission;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@RestController
public class CloudMembershipController {
    private final CloudMembershipService service;
    public CloudMembershipController(CloudMembershipService service) { this.service = service; }

    @PostMapping("/internal/memberships/{userId}/house-views/{houseId}")
    public HouseViewPermission consume(@PathVariable long userId, @PathVariable long houseId) { return service.consumeView(userId, houseId); }

    @PostMapping("/cloud/api/memberships/redeem")
    public void redeem(@RequestHeader("X-User-Id") long userId, @RequestBody RedeemRequest request) {
        service.redeem(userId, sha256(request.code().trim().toUpperCase()));
    }

    private static String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
    public record RedeemRequest(String code) { }
}
