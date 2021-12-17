package io.github.thebesteric.framework.switchlogger.test.service;

import com.dtflys.forest.annotation.*;

import java.util.Map;

// @BaseRequest(baseURL = "http://127.0.0.1:8020/account", interceptor = ForestInterceptor.class)
@BaseRequest(baseURL = "http://127.0.0.1:8020/account")
public interface AccountForestService {

    @Get(url = "/${id}")
    Object get(@Var("id") String id, @Query("name") String name, @Query("age") int age, @Body Map<String, Object> body);
}
