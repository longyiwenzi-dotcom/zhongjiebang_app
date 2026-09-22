package cn.hrbzhongjiebang.web;

import cn.hrbzhongjiebang.user.AuthResult;
import cn.hrbzhongjiebang.user.AuthService;
import cn.hrbzhongjiebang.user.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zhongjiebang-demo/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    AuthResult register(@Valid @RequestBody Credentials request) {
        return authService.register(request.phone(), request.password());
    }

    @PostMapping("/login/password")
    AuthResult login(@Valid @RequestBody Credentials request) {
        return authService.loginByPassword(request.phone(), request.password());
    }

    @PostMapping("/password")
    void changePassword(@AuthenticationPrincipal AuthenticatedUser user,
                        @Valid @RequestBody ChangePassword request) {
        authService.changePassword(user.id(), request.password());
    }

    public record Credentials(@NotBlank(message = "请输入手机号") String phone, String password) {}
    public record ChangePassword(@NotBlank(message = "请输入新密码") String password) {}
}
