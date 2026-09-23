package cn.hrbzhongjiebang.web;

import cn.hrbzhongjiebang.house.House;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record HouseView(
        long id, boolean rent, String community, String street, String building, String unit,
        Integer floor, Integer rooms, Integer halls, Integer bathrooms, BigDecimal buildingArea,
        BigDecimal usableArea, BigDecimal price, String paymentTerm, Boolean elevator,
        String decoration, String orientation, List<String> imageUrls, String videoLink,
        String uploaderPhone, String specificAddress, String landlordName, String landlordPhone,
        Instant createdAt) {

    static HouseView summary(House h) {
        List<String> cover = h.imageUrls().isEmpty() ? List.of() : List.of(h.imageUrls().get(0));
        return copy(h, cover, null, null, null, null, false);
    }

    static HouseView detail(House h, boolean owner) {
        return copy(h, h.imageUrls(), h.videoLink(), owner ? h.specificAddress() : null,
                owner ? h.landlordName() : null, owner ? h.landlordPhone() : null, true);
    }

    private static HouseView copy(House h, List<String> images, String video, String address,
                                  String landlordName, String landlordPhone, boolean detail) {
        return new HouseView(h.id(), h.rent(), h.community(), h.street(), h.building(), h.unit(),
                h.floor(), h.rooms(), h.halls(), h.bathrooms(), h.buildingArea(), h.usableArea(),
                h.price(), h.paymentTerm(), h.elevator(), h.decoration(), h.orientation(), images,
                video, detail ? h.uploaderPhone() : null, address, landlordName, landlordPhone, h.createdAt());
    }
}
