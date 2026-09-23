package cn.hrbzhongjiebang.cloud.house;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@org.springframework.context.annotation.Import(cn.hrbzhongjiebang.cloud.contracts.InternalAuthenticationConfiguration.class)
@SpringBootApplication
public class HouseApplication {
    public static void main(String[] args) { SpringApplication.run(HouseApplication.class, args); }
}
