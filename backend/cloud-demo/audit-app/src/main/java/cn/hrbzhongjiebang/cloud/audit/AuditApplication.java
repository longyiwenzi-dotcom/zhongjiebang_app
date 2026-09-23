package cn.hrbzhongjiebang.cloud.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.context.annotation.Import(cn.hrbzhongjiebang.cloud.contracts.InternalAuthenticationConfiguration.class)
@SpringBootApplication
public class AuditApplication {
    public static void main(String[] args) { SpringApplication.run(AuditApplication.class, args); }
}
