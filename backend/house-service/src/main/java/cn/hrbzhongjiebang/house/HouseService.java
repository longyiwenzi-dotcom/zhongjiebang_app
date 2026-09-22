package cn.hrbzhongjiebang.house;

import cn.hrbzhongjiebang.common.BusinessException;
import cn.hrbzhongjiebang.common.PageResult;
import cn.hrbzhongjiebang.user.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HouseService {
    private final HouseRepository houses;
    private final Clock clock;

    public HouseService(HouseRepository houses, Clock clock) {
        this.houses = houses;
        this.clock = clock;
    }

    @Transactional
    public House create(AuthenticatedUser user, CreateHouseCommand command) {
        if (command == null || blank(command.building()) || blank(command.unit()) || command.floor() == null) {
            throw new BusinessException("HOUSE_REQUIRED_FIELDS", "楼栋号、单元号和楼层不能为空");
        }
        if (command.price() == null || command.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_PRICE", "金额必须大于0");
        }
        if (command.imageUrls() != null && command.imageUrls().size() > 12) {
            throw new BusinessException("TOO_MANY_IMAGES", "最多上传12张照片");
        }
        return houses.insert(user.id(), user.phone(), command, clock.instant());
    }

    public PageResult<House> search(HouseSearch search) {
        return houses.search(search);
    }

    public House find(long id) {
        return houses.findById(id)
                .orElseThrow(() -> new BusinessException("HOUSE_NOT_FOUND", "房源不存在"));
    }

    @Transactional
    public House updateLandlordPhone(AuthenticatedUser user, long houseId, String phone) {
        if (phone != null && !phone.isBlank() && !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("INVALID_PHONE", "房东手机号格式不正确");
        }
        if (!houses.updateLandlordPhone(houseId, user.id(), phone == null ? "" : phone)) {
            throw new BusinessException("FORBIDDEN", "只能修改自己上传的房源");
        }
        return find(houseId);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
