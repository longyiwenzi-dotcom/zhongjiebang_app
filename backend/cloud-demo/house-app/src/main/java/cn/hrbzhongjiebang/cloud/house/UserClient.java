package cn.hrbzhongjiebang.cloud.house;

import cn.hrbzhongjiebang.cloud.contracts.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "zjb-user")
public interface UserClient {
    @GetMapping("/internal/users/{id}")
    UserSummary find(@PathVariable long id);
}
