package cn.hrbzhongjiebang.cloud.audit;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import cn.hrbzhongjiebang.cloud.contracts.HouseViewedEvent;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
class AuditIdempotencyTest {
 @Test void redeliveryCreatesOneHistoryRow() {
  var jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:audit;MODE=MySQL;DB_CLOSE_DELAY=-1","sa",""));
  jdbc.execute("create table cloud_house_view_history(event_id varchar(36) unique,user_id bigint,house_id bigint,viewed_at timestamp,trace_id varchar(64))");
  var service=new HouseViewAuditService(jdbc);var event=new HouseViewedEvent("event-1",1,2,Instant.now(),"trace");
  service.record(event);service.record(event);
  assertEquals(1,jdbc.queryForObject("select count(*) from cloud_house_view_history",Integer.class));
 }
}
