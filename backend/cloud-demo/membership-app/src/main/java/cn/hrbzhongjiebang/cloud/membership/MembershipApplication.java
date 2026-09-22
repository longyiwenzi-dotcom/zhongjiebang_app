package cn.hrbzhongjiebang.cloud.membership;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MembershipApplication {
    public static void main(String[] args) { SpringApplication.run(MembershipApplication.class, args); }
}
