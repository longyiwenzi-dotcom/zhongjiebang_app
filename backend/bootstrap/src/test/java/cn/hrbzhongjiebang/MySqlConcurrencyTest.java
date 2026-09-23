package cn.hrbzhongjiebang;

import cn.hrbzhongjiebang.user.*;
import cn.hrbzhongjiebang.membership.*;
import cn.hrbzhongjiebang.house.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker=true)
@SpringBootTest(properties={"spring.datasource.hikari.maximum-pool-size=16"})
class MySqlConcurrencyTest {
    @Container static final MySQLContainer<?> MYSQL=new MySQLContainer<>("mysql:8.4");
    @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",MYSQL::getJdbcUrl);r.add("spring.datasource.username",MYSQL::getUsername);r.add("spring.datasource.password",MYSQL::getPassword);
    }
    @Autowired AuthService auth; @Autowired MembershipService members; @Autowired HouseService houses; @Autowired JdbcTemplate jdbc;
    private AuthenticatedUser user(String phone){return auth.authenticate(auth.register(phone,"StrongPass123").token());}
    private String code(char suffix) throws Exception {
        String code="A".repeat(25)+suffix;
        String hash=HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(code.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        jdbc.update("insert into redeem_codes(code_hash,plan_code,batch_no,created_at) values (?,'WEEK','TEST',?)",hash,Instant.now());return code;
    }
    private int parallel(int count, java.util.function.IntConsumer action) throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(count);CountDownLatch ready=new CountDownLatch(count),start=new CountDownLatch(1);AtomicInteger success=new AtomicInteger();
        try {
            List<Future<?>> tasks=new ArrayList<>();
            for(int n=0;n<count;n++){final int i=n;tasks.add(pool.submit(()->{ready.countDown();try{start.await();action.accept(i);success.incrementAndGet();}catch(cn.hrbzhongjiebang.common.BusinessException expected){}catch(InterruptedException e){Thread.currentThread().interrupt();}}));}
            assertTrue(ready.await(20,TimeUnit.SECONDS));start.countDown();for(Future<?> task:tasks)task.get(40,TimeUnit.SECONDS);return success.get();
        } finally {pool.shutdownNow();}
    }
    @Test void oneCodeHasExactlyOneWinner() throws Exception {
        var a=user("13912340001");var b=user("13912340002");String code=code('B');
        assertEquals(1,parallel(2,i->members.redeem(i==0?a.id():b.id(),code)));
    }
    @Test void simultaneousDifferentCodesExtendTheSameUserTwice() throws Exception {
        var a=user("13912340003");String first=code('C'),second=code('D');
        assertEquals(2,parallel(2,i->members.redeem(a.id(),i==0?first:second)));
        assertTrue(members.status(a.id()).expiresAt().isAfter(Instant.now().plusSeconds(13*86400)));
    }
    @Test void parallelDistinctViewsStopAtTenAndRepeatDoesNotCharge() throws Exception {
        var owner=user("13912340004");var viewer=user("13912340005");members.redeem(viewer.id(),code('E'));
        List<Long> ids=new ArrayList<>();
        for(int i=0;i<14;i++)ids.add(houses.create(owner,new CreateHouseCommand(true,"test","street","1","1",1,1,1,1,new BigDecimal("60"),null,new BigDecimal("2000"),null,false,null,null,List.of(),null,"private",null,null)).id());
        assertEquals(10,parallel(14,i->members.authorizeHouseView(viewer.id(),owner.id(),ids.get(i))));
        Long viewed=jdbc.queryForObject("select house_id from house_view_records where user_id=? limit 1",Long.class,viewer.id());
        members.authorizeHouseView(viewer.id(),owner.id(),viewed);
        assertEquals(10,jdbc.queryForObject("select count(*) from house_view_records where user_id=?",Integer.class,viewer.id()));
        assertEquals(0,members.status(viewer.id()).remainingViews());
    }
}
