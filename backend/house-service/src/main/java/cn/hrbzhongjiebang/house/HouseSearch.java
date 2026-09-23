package cn.hrbzhongjiebang.house;

import java.math.BigDecimal;

public record HouseSearch(
        Boolean rent, String keyword, BigDecimal minPrice, BigDecimal maxPrice,
        Integer rooms, Boolean elevator, int page, int size) {
    public HouseSearch {
        page = Math.min(Math.max(page, 1), 1000000);
        size = Math.min(Math.max(size, 1), 50);
    }
}
