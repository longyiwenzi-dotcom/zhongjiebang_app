package cn.hrbzhongjiebang.house;

import java.math.BigDecimal;
import java.util.List;

public record CreateHouseCommand(
        boolean rent, String community, String street, String building, String unit,
        Integer floor, Integer rooms, Integer halls, Integer bathrooms,
        BigDecimal buildingArea, BigDecimal usableArea, BigDecimal price,
        String paymentTerm, Boolean elevator, String decoration, String orientation,
        List<String> imageUrls, String videoLink, String specificAddress,
        String landlordName, String landlordPhone) {
}
