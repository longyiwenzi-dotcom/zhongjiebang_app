package cn.hrbzhongjiebang.house;

import cn.hrbzhongjiebang.common.PageResult;
import java.time.Instant;
import java.util.Optional;

public interface HouseRepository {
    House insert(long ownerId, String uploaderPhone, CreateHouseCommand command, Instant now);

    Optional<House> findById(long id);

    PageResult<House> search(HouseSearch search);

    boolean updateLandlordPhone(long houseId, long ownerId, String phone);
}
