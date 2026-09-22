package cn.hrbzhongjiebang.web;

import cn.hrbzhongjiebang.common.PageResult;
import cn.hrbzhongjiebang.house.CreateHouseCommand;
import cn.hrbzhongjiebang.house.House;
import cn.hrbzhongjiebang.house.HouseSearch;
import cn.hrbzhongjiebang.house.HouseService;
import cn.hrbzhongjiebang.membership.MembershipService;
import cn.hrbzhongjiebang.user.AuthenticatedUser;
import java.math.BigDecimal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zhongjiebang-demo/api/v1/houses")
public class HouseController {
    private final HouseService houses;
    private final MembershipService memberships;

    public HouseController(HouseService houses, MembershipService memberships) {
        this.houses = houses;
        this.memberships = memberships;
    }

    @GetMapping
    PageResult<HouseView> search(@RequestParam(required = false) Boolean rent,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) BigDecimal minPrice,
                                 @RequestParam(required = false) BigDecimal maxPrice,
                                 @RequestParam(required = false) Integer rooms,
                                 @RequestParam(required = false) Boolean elevator,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size) {
        PageResult<House> result = houses.search(new HouseSearch(
                rent, keyword, minPrice, maxPrice, rooms, elevator, page, size));
        return new PageResult<>(result.items().stream().map(HouseView::summary).toList(),
                result.total(), result.page(), result.size());
    }

    @GetMapping("/{id}")
    HouseView detail(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable long id) {
        House house = houses.find(id);
        memberships.authorizeHouseView(user.id(), house.ownerId(), house.id());
        return HouseView.detail(house, user.id() == house.ownerId());
    }

    @PostMapping
    HouseView create(@AuthenticationPrincipal AuthenticatedUser user,
                     @RequestBody CreateHouseCommand command) {
        return HouseView.detail(houses.create(user, command), true);
    }

    @PutMapping("/{id}/landlord-phone")
    HouseView updateLandlordPhone(@AuthenticationPrincipal AuthenticatedUser user,
                                  @PathVariable long id, @RequestBody PhoneRequest request) {
        return HouseView.detail(houses.updateLandlordPhone(user, id, request.phone()), true);
    }

    public record PhoneRequest(String phone) {}
}
