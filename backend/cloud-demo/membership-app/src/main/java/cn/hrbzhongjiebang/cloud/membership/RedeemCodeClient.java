package cn.hrbzhongjiebang.cloud.membership;

import cn.hrbzhongjiebang.cloud.contracts.RedeemConsumeResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "zjb-redeem")
public interface RedeemCodeClient {
    @PostMapping("/internal/redeem-codes/consume")
    RedeemConsumeResult consume(@RequestBody ConsumeRequest request);
    record ConsumeRequest(long userId, String codeHash) { }
}
