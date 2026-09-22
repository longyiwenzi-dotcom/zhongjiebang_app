package cn.hrbzhongjiebang.cloud.house;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CloudHouseService {
    private final JdbcTemplate jdbc;
    public CloudHouseService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public long create(long ownerId, CreateHouse request) {
        validate(request);
        jdbc.update("insert into cloud_houses(owner_id,is_rent,community,street,building,unit_no,floor,building_area,usable_area,price,payment_term,image_urls,video_link,specific_address,landlord_phone) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                ownerId, request.rent(), blankToNull(request.community()), blankToNull(request.street()), request.building(), request.unit(),
                request.floor(), request.buildingArea(), request.usableArea(), request.price(), blankToNull(request.paymentTerm()),
                blankToNull(request.imageUrls()), blankToNull(request.videoLink()), blankToNull(request.specificAddress()), blankToNull(request.landlordPhone()));
        return jdbc.queryForObject("select last_insert_id()", Long.class);
    }

    public CloudHouse find(long id) {
        CloudHouse house = jdbc.query("select * from cloud_houses where id=? and status='ACTIVE'", result -> result.next() ? map(result) : null, id);
        if (house == null) throw new IllegalArgumentException("房源不存在");
        return house;
    }

    public List<CloudHouse> search(Boolean rent, String keyword, int page, int size) {
        int safePage = Math.max(0, page), safeSize = Math.max(1, Math.min(size, 50));
        String term = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        return jdbc.query("select * from cloud_houses where status='ACTIVE' and (? is null or is_rent=?) and (community like ? or street like ?) order by id desc limit ? offset ?",
                (result, row) -> map(result), rent, rent, term, term, safeSize, safePage * safeSize);
    }

    private static CloudHouse map(ResultSet r) throws SQLException {
        Timestamp created = r.getTimestamp("created_at");
        return new CloudHouse(r.getLong("id"), r.getLong("owner_id"), r.getBoolean("is_rent"), r.getString("community"),
                r.getString("street"), r.getString("building"), r.getString("unit_no"), (Integer) r.getObject("floor"),
                r.getBigDecimal("building_area"), r.getBigDecimal("usable_area"), r.getBigDecimal("price"),
                r.getString("payment_term"), r.getString("image_urls"), r.getString("video_link"),
                r.getString("specific_address"), r.getString("landlord_phone"), created.toInstant());
    }

    private static void validate(CreateHouse request) {
        if (request.building() == null || request.building().isBlank()) throw new IllegalArgumentException("楼栋号不能为空");
        if (request.unit() == null || request.unit().isBlank()) throw new IllegalArgumentException("单元号不能为空");
        if (request.floor() == null) throw new IllegalArgumentException("楼层不能为空");
        if (request.price() == null || request.price().signum() <= 0) throw new IllegalArgumentException("金额必须大于0");
    }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record CreateHouse(boolean rent, String community, String street, String building, String unit,
            Integer floor, java.math.BigDecimal buildingArea, java.math.BigDecimal usableArea,
            java.math.BigDecimal price, String paymentTerm, String imageUrls, String videoLink,
            String specificAddress, String landlordPhone) { }
}
