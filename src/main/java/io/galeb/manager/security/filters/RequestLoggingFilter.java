/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2017 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.manager.security.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class RequestLoggingFilter extends AbstractRequestLoggingFilter {

    private static final Log LOGGER = LogFactory.getLog(RequestLoggingFilter.class);


    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        final FilterRegistrationBean registration = new FilterRegistrationBean();
        RequestLoggingFilter loggingFilter = new RequestLoggingFilter();
        registration.setFilter(loggingFilter);
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        return registration;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean isFirstRequest = !isAsyncDispatch(request);
        long timeEnd = 0;
        try {
            long timeStart = System.currentTimeMillis();
            super.doFilterInternal(request, response, filterChain);
            timeEnd = System.currentTimeMillis() - timeStart;
        } finally {
            if (isFirstRequest) {
                logRequest(request, response, timeEnd);
            }
        }

    }

    private void logRequest(HttpServletRequest request, HttpServletResponse response, long time) {
        Map<String, String> mapContext = new HashMap<>();
        mapContext.put("uri", request.getRequestURI() + (request.getQueryString() == null ? "" : request.getQueryString()));
        mapContext.put("method", request.getMethod());
        String xForwardFor = request.getHeader("X-Forward-For");
        if (xForwardFor != null) {
            mapContext.put("client", xForwardFor);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            mapContext.put("user", authentication.getName());
        }
        mapContext.put("status", String.valueOf(response.getStatus()));
        mapContext.put("time", String.valueOf(time));
        String result = mapContext.entrySet()
                                  .stream()
                                  .map(entry -> entry.getKey() + ": " + entry.getValue())
                                  .collect(Collectors.joining(", "));
        ThreadContext.getContext().putAll(mapContext);
        LOGGER.info(result);
        ThreadContext.clearMap();
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {}

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {}

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        return false;
    }
}
