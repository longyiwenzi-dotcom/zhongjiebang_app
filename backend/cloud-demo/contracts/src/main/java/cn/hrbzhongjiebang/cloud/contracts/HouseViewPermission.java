package cn.hrbzhongjiebang.cloud.contracts;

public record HouseViewPermission(boolean allowed, String reason, int remainingViews) {
    public static HouseViewPermission denied(String reason) {
        return new HouseViewPermission(false, reason, 0);
    }
}
