package cn.hrbzhongjiebang.cloud.membership;

import cn.hrbzhongjiebang.cloud.contracts.HouseViewPermission;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@RestController
public class CloudMembershipController {
    private final CloudMembershipService service;
    public CloudMembershipController(CloudMembershipService service) { this.service = service; }

    @PostMapping("/internal/memberships/{userId}/house-views/{houseId}")
    public HouseViewPermission consume(@PathVariable long userId, @PathVariable long houseId) { return service.consumeView(userId, houseId); }

    @PostMapping("/cloud/api/memberships/redeem")
    public void redeem(@RequestHeader("X-User-Id") long userId, @RequestBody RedeemRequest request) {
        if (request.code() == null || !request.code().matches("[A-Za-z0-9]{26}")) throw new IllegalArgumentException("兑换码格式不正确");
        service.redeem(userId, sha256(request.code().trim().toUpperCase()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_REDEEM_CODE", "message", error.getMessage()));
    }

    private static String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
    public record RedeemRequest(String code) { }
}
