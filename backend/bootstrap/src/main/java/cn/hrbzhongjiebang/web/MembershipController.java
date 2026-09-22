package cn.hrbzhongjiebang.web;

import cn.hrbzhongjiebang.membership.MembershipService;
import cn.hrbzhongjiebang.membership.MembershipStatus;
import cn.hrbzhongjiebang.user.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zhongjiebang-demo/api/v1/membership")
public class MembershipController {
    private final MembershipService memberships;

    public MembershipController(MembershipService memberships) {
        this.memberships = memberships;
    }

    @GetMapping
    MembershipStatus status(@AuthenticationPrincipal AuthenticatedUser user) {
        return memberships.status(user.id());
    }

    @PostMapping("/redeem")
    MembershipStatus redeem(@AuthenticationPrincipal AuthenticatedUser user,
                            @RequestBody RedeemRequest request) {
        return memberships.redeem(user.id(), request.code());
    }

    public record RedeemRequest(String code) {}
}
