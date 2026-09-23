package cn.hrbzhongjiebang.cloud.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.context.annotation.Import(cn.hrbzhongjiebang.cloud.contracts.InternalAuthenticationConfiguration.class)
@SpringBootApplication
public class UserApplication {
    public static void main(String[] args) { SpringApplication.run(UserApplication.class, args); }
}
