package cn.hrbzhongjiebang.cloud.house;

import cn.hrbzhongjiebang.cloud.contracts.HouseViewedEvent;
import io.micrometer.tracing.Tracer;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class HouseViewEventPublisher {
    public static final String EXCHANGE = "zjb.house.events";
    private static final Logger log = LoggerFactory.getLogger(HouseViewEventPublisher.class);
    private final RabbitTemplate rabbit;
    private final Tracer tracer;

    public HouseViewEventPublisher(RabbitTemplate rabbit, Tracer tracer) { this.rabbit = rabbit; this.tracer = tracer; }

    public void viewed(long userId, long houseId) {
        String traceId = tracer.currentSpan() == null ? "" : tracer.currentSpan().context().traceId();
        try { rabbit.convertAndSend(EXCHANGE, "house.viewed", new HouseViewedEvent(userId, houseId, Instant.now(), traceId)); }
        catch (RuntimeException error) { log.warn("House view audit event could not be published: user={}, house={}", userId, houseId, error); }
    }
}
