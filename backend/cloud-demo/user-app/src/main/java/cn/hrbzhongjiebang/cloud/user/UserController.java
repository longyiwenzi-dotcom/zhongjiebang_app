package cn.hrbzhongjiebang.cloud.user;

import cn.hrbzhongjiebang.cloud.contracts.AuthSession;
import cn.hrbzhongjiebang.cloud.contracts.UserSummary;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    private final CloudUserService service;
    public UserController(CloudUserService service) { this.service = service; }

    @PostMapping("/cloud/api/users/register")
    public AuthSession register(@RequestBody Credentials request) { return service.register(request.phone(), request.password()); }

    @PostMapping("/cloud/api/users/login")
    public AuthSession login(@RequestBody Credentials request) { return service.login(request.phone(), request.password()); }

    @GetMapping("/cloud/api/users/me")
    public UserSummary me(@RequestHeader("X-User-Id") long userId) { return service.find(userId); }

    @GetMapping("/internal/users/{id}")
    public UserSummary summary(@PathVariable long id) {
        return service.find(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_REQUEST", "message", error.getMessage()));
    }

    public record Credentials(String phone, String password) { }
}
