package cn.hrbzhongjiebang.cloud.redeem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.context.annotation.Import(cn.hrbzhongjiebang.cloud.contracts.InternalAuthenticationConfiguration.class)
@SpringBootApplication
public class RedeemApplication {
    public static void main(String[] args) { SpringApplication.run(RedeemApplication.class, args); }
}
