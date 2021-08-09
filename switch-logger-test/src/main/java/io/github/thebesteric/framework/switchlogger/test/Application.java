package io.github.thebesteric.framework.switchlogger.test;

import io.github.thebesteric.framework.switchlogger.annotation.EnableSwitchLogger;
import io.github.thebesteric.framework.switchlogger.core.processor.GlobalSuccessResponseProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableSwitchLogger
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // @Bean
    public GlobalSuccessResponseProcessor globalSuccessResponseProcessor() {
        return (method, result) -> "you can check result and return error message";
    }

    @RestControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler(value = Exception.class)
        public Map<String, Object> exceptionHandler(Exception e) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 500);
            map.put("msg", "未知异常！原因是:" + e);
            map.put("data", null);
            return map;
        }
    }
}
