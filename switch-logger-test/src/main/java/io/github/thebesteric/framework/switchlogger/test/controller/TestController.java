package io.github.thebesteric.framework.switchlogger.test.controller;

import io.github.thebesteric.framework.switchlogger.test.service.AccountFeignService;
import io.github.thebesteric.framework.switchlogger.test.service.AccountForestService;
import io.github.thebesteric.framework.switchlogger.test.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private TestService testService;

    @Autowired
    private AccountFeignService feignService;

    @Autowired
    private AccountForestService forestService;


    @GetMapping("/calc/{divisor}")
    public Map<String, Object> calc(@PathVariable int divisor) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", "succeed");
        map.put("data", testService.div(10, divisor));
        return map;
    }

    @GetMapping("/feign/account/get/{id}")
    public Object accountGet(@PathVariable String id, @RequestParam String name, @RequestParam int age,
                             @RequestBody Map<String, Object> body) {
        return feignService.get(id, name, age, body);
    }

    @PostMapping("/forest/account/get/{id}")
    public Object forestAccountGet(@PathVariable String id, @RequestParam String name, @RequestParam int age,
                                   @RequestBody Map<String, Object> body) {
        // return Forest.get("http://127.0.0.1:8020/account/" + id).executeAsString();
        return forestService.get(id, name, age, body);
    }

}
