package cn.hrbzhongjiebang.cloud.contracts;

import java.time.Instant;

public record HouseViewedEvent(String eventId, long userId, long houseId, Instant viewedAt, String traceId) { }
