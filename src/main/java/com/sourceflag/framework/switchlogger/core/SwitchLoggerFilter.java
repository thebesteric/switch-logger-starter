package com.sourceflag.framework.switchlogger.core;

import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerFilterWrapper;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerRequestWrapper;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerResponseWrapper;
import com.sourceflag.framework.switchlogger.starter.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.utils.DurationWatch;
import com.sourceflag.framework.switchlogger.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * SwitchLoggerFilter
 *
 * @author Eric Joe
 * @version 1.0
 * @date 2020-09-29 23:15
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SwitchLoggerFilter extends SwitchLoggerFilterWrapper {

    private final SwitchLoggerProperties properties;

    private final List<RecordProcessor> recordProcessors;

    private static final Set<String> IGNORE_URIS = new HashSet<>();

    static {
        IGNORE_URIS.add("/favicon.ico");
        IGNORE_URIS.add("/switch-logger/query");
    }

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        if (!properties.isEnable()) {
            filterChain.doFilter(request, response);
            return;
        }

        // check ignore uri
        String requestURI = ((HttpServletRequest) request).getRequestURI();
        for (String ignoreUri : IGNORE_URIS) {
            if (requestURI.startsWith(ignoreUri)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // check uri legal
        if (checkLegalUri(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // wrapper request & response
        SwitchLoggerRequestWrapper requestWrapper = new SwitchLoggerRequestWrapper((HttpServletRequest) request);
        SwitchLoggerResponseWrapper responseWrapper = new SwitchLoggerResponseWrapper((HttpServletResponse) response);

        // record spend time
        DurationWatch.start();
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception ex) {
            ex.printStackTrace();
            responseWrapper.setBuffer(ex.getMessage());
        }

        long duration = DurationWatch.stop();

        // record request info
        RequestLog requestLog = new RequestLog(requestWrapper, responseWrapper);
        try {
            requestLog.setResult(JsonUtils.mapper.readTree(requestLog.getResult().toString()));
        } catch (Exception ex) {
            log.debug("cannot parse {} to json", requestLog.getResult());
        }

        Method method = URL_MAPPING.get(requestLog.getUri());
        if (method != null) {
            requestLog.setExecuteInfo(new RequestLog.ExecuteInfo(method, DurationWatch.getStartTime(), duration));
        }

        // recorder request log
        for (RecordProcessor recordProcessor : recordProcessors) {
            if (recordProcessor.supports(properties.getModel())) {
                doProcessor(recordProcessor, requestLog);
                break;
            }
        }

        ServletOutputStream out = response.getOutputStream();
        out.write(responseWrapper.getByteArray());
        out.flush();

    }

    public boolean checkLegalUri(String targetUri) {
        return !doCheckLegalUri(targetUri, true) && doCheckLegalUri(targetUri, false);
    }

    public boolean doCheckLegalUri(String targetUri, boolean include) {
        boolean passed = false;
        String[] uris = include ? properties.getFilter().getInclude() : properties.getFilter().getExclude();
        if (uris != null && uris.length > 0) {
            boolean[] excludes = new boolean[uris.length];
            for (int i = 0; i < uris.length; i++) {
                excludes[i] = passed = Pattern.matches(uris[i], targetUri);
                if (passed && include) {
                    break;
                }
            }

            if (!include) {
                for (boolean exclude : excludes) {
                    if (exclude) {
                        passed = false;
                        break;
                    }
                }
            }
        }

        return passed;
    }

    public void doProcessor(RecordProcessor recordProcessor, RequestLog requestLog) {
        CompletableFuture.runAsync(() -> {
            try {
                recordProcessor.processor(requestLog);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
