package io.github.thebesteric.framework.switchlogger.test.service;

import io.github.thebesteric.framework.switchlogger.annotation.SwitchLogger;
import org.springframework.stereotype.Service;

@SwitchLogger
@Service
public class TestService {
    public double div(double x, double y) {
        if (y == 0) {
            throw new RuntimeException("divisor cannot be 0");
        }
        return x / y;
    }
}
