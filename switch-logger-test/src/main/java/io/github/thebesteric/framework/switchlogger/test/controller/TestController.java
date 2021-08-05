package io.github.thebesteric.framework.switchlogger.test.controller;

import io.github.thebesteric.framework.switchlogger.test.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/calc/{divisor}")
    public Map<String, Object> calc(@PathVariable int divisor) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", "succeed");
        map.put("data", testService.div(10, divisor));
        return map;
    }
}
