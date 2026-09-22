package cn.hrbzhongjiebang.cloud.house;

import cn.hrbzhongjiebang.cloud.contracts.HouseViewPermission;
import cn.hrbzhongjiebang.cloud.contracts.UserSummary;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cloud/api/houses")
public class CloudHouseController {
    private final CloudHouseService houses;
    private final MembershipClient memberships;
    private final UserClient users;
    private final HouseViewEventPublisher events;

    public CloudHouseController(CloudHouseService houses, MembershipClient memberships, UserClient users, HouseViewEventPublisher events) {
        this.houses = houses; this.memberships = memberships; this.users = users; this.events = events;
    }

    @PostMapping
    public Map<String, Long> create(@RequestHeader("X-User-Id") long userId, @RequestBody CloudHouseService.CreateHouse request) {
        return Map.of("id", houses.create(userId, request));
    }

    @GetMapping
    public List<CloudHouse> search(@RequestParam(required = false) Boolean rent,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return houses.search(rent, keyword, page, size).stream().map(CloudHouseController::publicListing).toList();
    }

    @GetMapping("/{houseId}")
    public ResponseEntity<?> detail(@PathVariable long houseId, @RequestHeader("X-User-Id") long userId) {
        CloudHouse house = houses.find(houseId);
        if (house.ownerId() != userId) {
            HouseViewPermission permission = memberships.consumeView(userId, houseId);
            if (!permission.allowed()) return ResponseEntity.status(403).body(permission);
        }
        UserSummary owner = users.find(house.ownerId());
        events.viewed(userId, houseId);
        return ResponseEntity.ok(new HouseDetail(house.id(), house.ownerId(), house.rent(), house.community(), house.street(),
                house.building(), house.unit(), house.floor(), house.buildingArea(), house.usableArea(), house.price(),
                house.paymentTerm(), house.imageUrls(), house.videoLink(), house.ownerId() == userId ? house.specificAddress() : null,
                house.ownerId() == userId ? house.landlordPhone() : null, owner.maskedPhone(), house.createdAt()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> badRequest(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_HOUSE", "message", error.getMessage()));
    }

    private static CloudHouse publicListing(CloudHouse h) {
        return new CloudHouse(h.id(), h.ownerId(), h.rent(), h.community(), h.street(), h.building(), h.unit(), h.floor(),
                h.buildingArea(), h.usableArea(), h.price(), h.paymentTerm(), h.imageUrls(), h.videoLink(), null, null, h.createdAt());
    }

    record HouseDetail(long id, long ownerId, boolean rent, String community, String street, String building,
            String unit, Integer floor, java.math.BigDecimal buildingArea, java.math.BigDecimal usableArea,
            java.math.BigDecimal price, String paymentTerm, String imageUrls, String videoLink,
            String specificAddress, String landlordPhone, String uploaderPhone, java.time.Instant createdAt) { }
}
