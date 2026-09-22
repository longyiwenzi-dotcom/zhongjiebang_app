package cn.hrbzhongjiebang.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoPageController {
    @GetMapping("/zhongjiebang-demo/")
    String demoHome() {
        return "forward:/zhongjiebang-demo/index.html";
    }
}
