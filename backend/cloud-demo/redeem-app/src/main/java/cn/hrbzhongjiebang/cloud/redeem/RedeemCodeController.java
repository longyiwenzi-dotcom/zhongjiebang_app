package cn.hrbzhongjiebang.cloud.redeem;

import cn.hrbzhongjiebang.cloud.contracts.RedeemConsumeResult;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/redeem-codes")
public class RedeemCodeController {
    private final RedeemCodeService service;
    public RedeemCodeController(RedeemCodeService service) { this.service = service; }

    @PostMapping("/consume")
    public RedeemConsumeResult consume(@RequestBody ConsumeRequest request) { return service.consume(request.userId(), request.codeHash()); }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> invalid(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_REDEEM_CODE", "message", error.getMessage()));
    }
    public record ConsumeRequest(long userId, String codeHash) { }
}
