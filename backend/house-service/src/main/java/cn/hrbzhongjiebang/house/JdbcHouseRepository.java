package cn.hrbzhongjiebang.house;

import cn.hrbzhongjiebang.common.PageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcHouseRepository implements HouseRepository {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final ObjectMapper objectMapper;

    public JdbcHouseRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.namedJdbc = namedJdbc;
        this.objectMapper = objectMapper;
    }

    @Override
    public House insert(long ownerId, String uploaderPhone, CreateHouseCommand c, Instant now) {
        KeyHolder key = new GeneratedKeyHolder();
        namedJdbc.update("""
                INSERT INTO houses(owner_id, is_rent, community, street, building_no, unit_no, floor_no,
                  rooms, halls, bathrooms, building_area, usable_area, price, payment_term, has_elevator,
                  decoration, orientation, image_urls, video_link, specific_address, landlord_name,
                  landlord_phone, uploader_phone, created_at)
                VALUES (:ownerId, :rent, :community, :street, :building, :unit, :floor, :rooms, :halls,
                  :bathrooms, :buildingArea, :usableArea, :price, :paymentTerm, :elevator, :decoration,
                  :orientation, :images, :videoLink, :specificAddress, :landlordName, :landlordPhone,
                  :uploaderPhone, :createdAt)
                """, houseParameters(ownerId, uploaderPhone, c, now), key, new String[]{"id"});
        return findById(key.getKey().longValue()).orElseThrow();
    }

    @Override
    public Optional<House> findById(long id) {
        return jdbc.query("SELECT * FROM houses WHERE id = ? AND status = 'ACTIVE'", this::map, id)
                .stream().findFirst();
    }

    @Override
    public PageResult<House> search(HouseSearch search) {
        StringBuilder where = new StringBuilder(" WHERE status = 'ACTIVE'");
        Map<String, Object> params = new HashMap<>();
        if (search.rent() != null) {
            where.append(" AND is_rent = :rent");
            params.put("rent", search.rent());
        }
        if (search.keyword() != null && !search.keyword().isBlank()) {
            where.append(" AND (community LIKE :keyword OR street LIKE :keyword)");
            params.put("keyword", "%" + search.keyword().trim() + "%");
        }
        if (search.minPrice() != null) {
            where.append(" AND price >= :minPrice");
            params.put("minPrice", search.minPrice());
        }
        if (search.maxPrice() != null) {
            where.append(" AND price <= :maxPrice");
            params.put("maxPrice", search.maxPrice());
        }
        if (search.rooms() != null) {
            where.append(" AND rooms = :rooms");
            params.put("rooms", search.rooms());
        }
        if (search.elevator() != null) {
            where.append(" AND has_elevator = :elevator");
            params.put("elevator", search.elevator());
        }
        long total = namedJdbc.queryForObject("SELECT COUNT(*) FROM houses" + where, params, Long.class);
        params.put("limit", search.size());
        params.put("offset", (search.page() - 1) * search.size());
        List<House> items = namedJdbc.query(
                "SELECT * FROM houses" + where + " ORDER BY created_at DESC, id DESC LIMIT :limit OFFSET :offset",
                params, this::map);
        return new PageResult<>(items, total, search.page(), search.size());
    }

    @Override
    public boolean updateLandlordPhone(long houseId, long ownerId, String phone) {
        return jdbc.update("UPDATE houses SET landlord_phone = ? WHERE id = ? AND owner_id = ?",
                phone, houseId, ownerId) == 1;
    }

    private MapSqlParameterSource houseParameters(long ownerId, String uploaderPhone, CreateHouseCommand c, Instant now) {
        Map<String, Object> values = new HashMap<>();
        values.put("ownerId", ownerId); values.put("rent", c.rent());
        values.put("community", c.community()); values.put("street", c.street());
        values.put("building", c.building()); values.put("unit", c.unit()); values.put("floor", c.floor());
        values.put("rooms", c.rooms()); values.put("halls", c.halls()); values.put("bathrooms", c.bathrooms());
        values.put("buildingArea", c.buildingArea()); values.put("usableArea", c.usableArea());
        values.put("price", c.price()); values.put("paymentTerm", c.paymentTerm());
        values.put("elevator", c.elevator()); values.put("decoration", c.decoration());
        values.put("orientation", c.orientation()); values.put("images", writeImages(c.imageUrls()));
        values.put("videoLink", c.videoLink()); values.put("specificAddress", c.specificAddress());
        values.put("landlordName", c.landlordName()); values.put("landlordPhone", c.landlordPhone());
        values.put("uploaderPhone", uploaderPhone); values.put("createdAt", Timestamp.from(now));
        return new MapSqlParameterSource(values);
    }

    private House map(ResultSet rs, int row) throws SQLException {
        return new House(rs.getLong("id"), rs.getLong("owner_id"), rs.getBoolean("is_rent"),
                rs.getString("community"), rs.getString("street"), rs.getString("building_no"),
                rs.getString("unit_no"), nullableInt(rs, "floor_no"), nullableInt(rs, "rooms"),
                nullableInt(rs, "halls"), nullableInt(rs, "bathrooms"), rs.getBigDecimal("building_area"),
                rs.getBigDecimal("usable_area"), rs.getBigDecimal("price"), rs.getString("payment_term"),
                nullableBoolean(rs, "has_elevator"), rs.getString("decoration"), rs.getString("orientation"),
                readImages(rs.getString("image_urls")), rs.getString("video_link"),
                rs.getString("specific_address"), rs.getString("landlord_name"), rs.getString("landlord_phone"),
                rs.getString("uploader_phone"), rs.getTimestamp("created_at").toInstant());
    }

    private Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private Boolean nullableBoolean(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        return rs.wasNull() ? null : value;
    }

    private String writeImages(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images == null ? List.of() : images);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid image list", e);
        }
    }

    private List<String> readImages(String value) {
        if (value == null || value.isBlank()) return List.of();
        try {
            return objectMapper.readValue(value, new TypeReference<ArrayList<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
