package com.sourceflag.framework.switchlogger.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sourceflag.framework.switchlogger.annotation.Column;
import com.sourceflag.framework.switchlogger.annotation.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * InvokeLog
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-12-11 14:54
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@Table(name = "invoke")
public class InvokeLog extends AbstractEntity {

    public static final String TYPE_INFO = "info";
    public static final String TYPE_ERROR = "error";

    @Column(length = 64)
    protected String tag = "default";

    @Column(length = 64)
    protected String type = TYPE_INFO;

    // to tracking controller -> method_1 -> method_2 -> ... 's link
    @JsonProperty("track_id")
    @Column(length = 64)
    protected String trackId;

    @JsonProperty("created_time")
    @Column(length = 20, type = "bigint")
    protected long createdTime;

    @JsonProperty("execute_info")
    @Column(type = "json")
    protected ExecuteInfo executeInfo;

    @Column(type = "json")
    protected Object result;

    @Column(length = 256)
    protected String exception;

    @Column(type = "json")
    protected Object extra;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExecuteInfo extends AbstractEntity {

        public ExecuteInfo(Method method, Object[] args, long startTime, long duration) {
            this.className = method.getDeclaringClass().getName();
            this.methodInfo = new MethodInfo(method, args);
            this.startTime = startTime;
            this.duration = duration;
        }

        private String className;

        private MethodInfo methodInfo;

        @JsonProperty("start_time")
        private long startTime;

        private long duration;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class MethodInfo extends AbstractEntity {

            private String methodName;
            private LinkedHashMap<String, Object> signatures = new LinkedHashMap<>();
            private LinkedHashMap<String, Object> arguments = new LinkedHashMap<>();
            private String returnType;

            public MethodInfo(Method method, Object[] args) {
                this.methodName = method.getName();
                Parameter[] params = method.getParameters();
                if (params != null && params.length > 0) {
                    for (int i = 0; i < params.length; i++) {
                        Parameter param = params[i];
                        signatures.put(param.getName(), param.getParameterizedType().getTypeName());
                        if (args != null) {
                            arguments.put(param.getName(), args[i]);
                        }
                    }
                }
                this.returnType = method.getReturnType().getName();
            }

        }

    }

}
