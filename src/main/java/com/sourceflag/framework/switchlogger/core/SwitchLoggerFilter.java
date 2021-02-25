package com.sourceflag.framework.switchlogger.core;

import com.sourceflag.framework.switchlogger.configuration.SwitchLoggerProperties;
import com.sourceflag.framework.switchlogger.core.domain.InvokeLog;
import com.sourceflag.framework.switchlogger.core.domain.RequestLog;
import com.sourceflag.framework.switchlogger.core.processor.IgnoreUrlProcessor;
import com.sourceflag.framework.switchlogger.core.processor.RecordProcessor;
import com.sourceflag.framework.switchlogger.core.processor.RequestLoggerProcessor;
import com.sourceflag.framework.switchlogger.core.processor.logger.DefaultRequestLoggerProcessor;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerFilterWrapper;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerRequestWrapper;
import com.sourceflag.framework.switchlogger.core.wrapper.SwitchLoggerResponseWrapper;
import com.sourceflag.framework.switchlogger.utils.DurationWatch;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
public class SwitchLoggerFilter extends SwitchLoggerFilterWrapper {

    private final SwitchLoggerProperties properties;
    private final List<RecordProcessor> recordProcessors;
    private final IgnoreUrlProcessor ignoreUrlProcessor;

    private RequestLoggerProcessor requestLoggerProcessor;

    public SwitchLoggerFilter(SwitchLoggerProperties properties, List<RecordProcessor> recordProcessors,
                              RequestLoggerProcessor requestLoggerProcessor, IgnoreUrlProcessor ignoreUrlProcessor) {
        this.properties = properties;
        this.recordProcessors = recordProcessors;
        this.requestLoggerProcessor = requestLoggerProcessor;
        this.ignoreUrlProcessor = generateIgnoreUrlProcessor(ignoreUrlProcessor);
    }

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        if (!properties.isEnable()) {
            filterChain.doFilter(request, response);
            return;
        }

        // check ignore uri
        String uri = ((HttpServletRequest) request).getRequestURI();
        for (String ignoreUri : ignoreUrlProcessor.get()) {
            if (uri.startsWith(ignoreUri)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // check uri legal
        if (checkLegalUri(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // wrapper request & response
        SwitchLoggerRequestWrapper requestWrapper = new SwitchLoggerRequestWrapper((HttpServletRequest) request);
        SwitchLoggerResponseWrapper responseWrapper = new SwitchLoggerResponseWrapper((HttpServletResponse) response);

        // record spend time
        DurationWatch.start();
        try {
            initTrackId(requestWrapper);
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception ex) {
            ex.printStackTrace();
            responseWrapper.setLevel(RequestLog.LEVEL_ERROR);
            responseWrapper.setException(ex);
            responseWrapper.setBuffer(ex.getMessage());
        }
        long duration = DurationWatch.stop();

        // record request info
        RequestLog requestLog = generateRequestLoggerProcessor().processor(requestWrapper, responseWrapper, URL_MAPPING, trackIdThreadLocal, duration);
        requestLog.setTag(RequestLog.DEFAULT_TAG);

        // recorder request log
        for (RecordProcessor recordProcessor : recordProcessors) {
            if (recordProcessor.supports(properties.getModel())) {
                recordProcessor.processor(requestLog);
                break;
            }
        }

        ServletOutputStream out = response.getOutputStream();
        out.write(responseWrapper.getByteArray());
        out.flush();

    }

    private RequestLoggerProcessor generateRequestLoggerProcessor() {
        if (requestLoggerProcessor == null) {
            requestLoggerProcessor = new DefaultRequestLoggerProcessor();
        }
        return requestLoggerProcessor;
    }

    private IgnoreUrlProcessor generateIgnoreUrlProcessor(IgnoreUrlProcessor ignoreUrlProcessor) {
        if (ignoreUrlProcessor == null) {
            ignoreUrlProcessor = this::addIgnoreUrls;
        } else {
            Set<String> ignoreUrls = ignoreUrlProcessor.get();
            addIgnoreUrls(ignoreUrls);
            ignoreUrlProcessor.add(ignoreUrls);
        }
        return ignoreUrlProcessor;
    }

    private void addIgnoreUrls(Set<String> ignoreUrls) {
        ignoreUrls.add("/favicon.ico");
        ignoreUrls.add("/switch-logger/query");
    }

    private boolean checkLegalUri(String targetUri) {
        return !doCheckLegalUri(targetUri, true) && doCheckLegalUri(targetUri, false);
    }

    private boolean doCheckLegalUri(String targetUri, boolean include) {
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

    @Deprecated
    private void doProcessor(RecordProcessor recordProcessor, InvokeLog invokeLog) {
        CompletableFuture.runAsync(() -> {
            try {
                recordProcessor.processor(invokeLog);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

}
