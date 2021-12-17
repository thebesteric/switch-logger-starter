package io.github.thebesteric.framework.switchlogger.test.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "mall-account", path = "/account")
public interface AccountFeignService {

    @GetMapping("/{id}")
    Object get(@PathVariable String id, @RequestParam String name, @RequestParam int age, @RequestBody Map<String, Object> body);

}
