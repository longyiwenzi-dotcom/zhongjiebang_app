package cn.hrbzhongjiebang.cloud.user;

import cn.hrbzhongjiebang.cloud.contracts.UserSummary;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @GetMapping("/cloud/api/users/{id}")
    public UserSummary find(@PathVariable long id) { return summary(id); }

    @GetMapping("/internal/users/{id}")
    public UserSummary summary(@PathVariable long id) {
        return new UserSummary(id, "181****1175", true);
    }
}
