package cn.hrbzhongjiebang.cloud.house;

import java.math.BigDecimal;
import java.time.Instant;

public record CloudHouse(long id, long ownerId, boolean rent, String community, String street,
        String building, String unit, Integer floor, BigDecimal buildingArea, BigDecimal usableArea,
        BigDecimal price, String paymentTerm, String imageUrls, String videoLink,
        String specificAddress, String landlordPhone, Instant createdAt) { }
