package cn.hrbzhongjiebang.user;

public record AuthResult(String token, String phone, boolean hasPassword) {
}
