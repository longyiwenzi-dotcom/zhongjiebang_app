package cn.hrbzhongjiebang.cloud.audit;

import cn.hrbzhongjiebang.cloud.contracts.HouseViewedEvent;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class HouseViewAuditService {
    private final JdbcTemplate jdbc;
    public HouseViewAuditService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @RabbitListener(queues = AuditConfiguration.QUEUE)
    public void record(HouseViewedEvent event) {
        if (event.eventId() == null || event.eventId().isBlank()) throw new IllegalArgumentException("eventId required");
        try { jdbc.update("insert into cloud_house_view_history(event_id,user_id,house_id,viewed_at,trace_id) values (?,?,?,?,?)",
                event.eventId(), event.userId(), event.houseId(), Timestamp.from(event.viewedAt()), event.traceId());
        } catch (org.springframework.dao.DuplicateKeyException duplicate) { /* same event already committed */ }
    }

    public List<Map<String, Object>> history(long userId, int limit) {
        return jdbc.queryForList("select house_id, viewed_at from cloud_house_view_history where user_id=? order by viewed_at desc limit ?",
                userId, Math.max(1, Math.min(limit, 100)));
    }
}
