package cn.hrbzhongjiebang.security;

public interface AttemptLimiter {
    void check(String action, String subject, int limit, long windowSeconds);
}
