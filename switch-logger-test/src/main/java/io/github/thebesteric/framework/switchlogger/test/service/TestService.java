package io.github.thebesteric.framework.switchlogger.test.service;

import io.github.thebesteric.framework.switchlogger.annotation.SwitchLogger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@SwitchLogger
@Service
public class TestService {
    public Map<String, Object> div(double x, double y) {
        if (y == 0) {
            throw new RuntimeException("divisor cannot be 0");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("code", 100);
        params.put("message", "ok!");
        return params;
    }
}
