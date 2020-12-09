package com.sourceflag.framework.switchlogger.core.processor;

import java.util.HashSet;
import java.util.Set;

/**
 * IgnoreUrlProcessor
 *
 * @author Eric
 * @date 2020/12/9 18:12
 */
public interface IgnoreUrlProcessor {

    Set<String> IGNORE_URLS = new HashSet<>(16);

    /**
     * add
     *
     * @param ignoreUrls ignoreUrls
     */
    void add(Set<String> ignoreUrls);

    /**
     * get
     *
     * @return java.util.Set<java.lang.String>
     */
    default Set<String> get() {
        return IGNORE_URLS;
    }

}
