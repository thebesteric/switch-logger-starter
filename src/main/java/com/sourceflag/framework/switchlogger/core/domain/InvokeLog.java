package com.sourceflag.framework.switchlogger.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sourceflag.framework.switchlogger.annotation.Column;
import com.sourceflag.framework.switchlogger.annotation.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;

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

    public static final String LEVEL_INFO = "INFO";
    public static final String LEVEL_ERROR = "ERROR";
    public static final String LEVEL_WARN = "WARN";

    public static final String DEFAULT_TAG = "default";

    @Column(length = 64)
    protected String tag = DEFAULT_TAG;

    @Column(length = 64)
    protected String level = LEVEL_INFO;

    /** to tracking controller -> method_1 -> method_2 -> ... 's link */
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
                            if (simplicityInTreatment(param)) {
                                arguments.put(param.getName(), param.getParameterizedType().getTypeName());
                            } else {
                                arguments.put(param.getName(), args[i]);
                            }
                        }
                    }
                }
                this.returnType = method.getReturnType().getName();
            }

            private boolean simplicityInTreatment(Parameter param) {
                return ServletRequest.class.isAssignableFrom(param.getType())
                        || ServletResponse.class.isAssignableFrom(param.getType())
                        || MultipartFile.class.isAssignableFrom(param.getType())
                        || File.class.isAssignableFrom(param.getType())
                        || InputStream.class.isAssignableFrom(param.getType())
                        || OutputStream.class.isAssignableFrom(param.getType());
            }
        }

    }


}
