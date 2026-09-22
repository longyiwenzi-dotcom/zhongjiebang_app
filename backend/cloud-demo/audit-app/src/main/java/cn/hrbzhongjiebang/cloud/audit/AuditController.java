package cn.hrbzhongjiebang.cloud.audit;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cloud/api/audit")
public class AuditController {
    private final HouseViewAuditService service;
    public AuditController(HouseViewAuditService service) { this.service = service; }
    @GetMapping("/history")
    public List<Map<String, Object>> history(@RequestHeader("X-User-Id") long userId,
            @RequestParam(defaultValue = "50") int limit) { return service.history(userId, limit); }
}
