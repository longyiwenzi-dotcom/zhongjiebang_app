package cn.hrbzhongjiebang.cloud.contracts;

public record AuthSession(String accessToken, long userId, long expiresInSeconds) { }
