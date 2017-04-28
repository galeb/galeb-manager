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
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Enumeration;

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
        StringBuilder sb = new StringBuilder();
        sb.append("request\turi:\t")
          .append(request.getRequestURI())
          .append(request.getQueryString() == null ? "" : request.getQueryString())
          .append("\tmethod:\t")
          .append(request.getMethod())
          .append("\tclient:\t")
          .append(request.getRemoteAddr());
        HttpSession session = request.getSession(false);
        if (session != null) {
            sb.append("\tsession:\t").append(session.getId());
        }
        String user = request.getRemoteUser();
        if (user != null) {
            sb.append("\tuser:\t").append(user);
        }
        sb.append("\tstatusCode:\t")
          .append(response.getStatus())
          .append("\ttime:\t")
          .append(time);
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            sb.append("\theaders:\t");
            while (headerNames.hasMoreElements()) {
                String next = headerNames.nextElement();
                sb.append(next)
                  .append(": ")
                  .append(request.getHeader(next))
                  .append(", ");
            }
            sb.delete(sb.length()-2,sb.length());
        }
        LOGGER.info(sb.toString());
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
