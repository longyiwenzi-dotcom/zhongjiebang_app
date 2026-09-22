package cn.hrbzhongjiebang.web;

import cn.hrbzhongjiebang.user.AuthResult;
import cn.hrbzhongjiebang.user.AuthService;
import cn.hrbzhongjiebang.user.AuthenticatedUser;
import cn.hrbzhongjiebang.security.AttemptLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zhongjiebang-demo/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final AttemptLimiter attemptLimiter;

    public AuthController(AuthService authService, AttemptLimiter attemptLimiter) {
        this.authService = authService;
        this.attemptLimiter = attemptLimiter;
    }

    @PostMapping("/register")
    AuthResult register(@Valid @RequestBody Credentials request, HttpServletRequest servletRequest) {
        attemptLimiter.check("register", clientKey(request.phone(), servletRequest), 5, 600);
        return authService.register(request.phone(), request.password());
    }

    @PostMapping("/login/password")
    AuthResult login(@Valid @RequestBody Credentials request, HttpServletRequest servletRequest) {
        attemptLimiter.check("password-login", clientKey(request.phone(), servletRequest), 10, 300);
        return authService.loginByPassword(request.phone(), request.password());
    }

    @PostMapping("/password")
    void changePassword(@AuthenticationPrincipal AuthenticatedUser user,
                        @Valid @RequestBody ChangePassword request) {
        authService.changePassword(user.id(), request.password());
    }

    public record Credentials(@NotBlank(message = "请输入手机号") String phone, String password) {}
    public record ChangePassword(@NotBlank(message = "请输入新密码") String password) {}

    private static String clientKey(String phone, HttpServletRequest request) {
        String value = request.getRemoteAddr() + ':' + phone.replaceAll("[^0-9]", "");
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception error) {
            throw new IllegalStateException("SHA-256 unavailable", error);
        }
    }
}
