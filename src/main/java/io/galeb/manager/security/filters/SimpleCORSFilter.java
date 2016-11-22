package io.galeb.manager.security.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCORSFilter implements Filter {

    private enum SystemEnvCors {
        CORS_ALLOW_ORIGIN,
        CORS_ALLOW_METHODS,
        CORS_ALLOW_HEADERS,
        CORS_MAX_AGE
    }

    private static final Log LOGGER = LogFactory.getLog(SimpleCORSFilter.class);
    private static final String DEFAULT_ALLOW_ORIGIN = "*";
    private static final String DEFAULT_ALLOW_METHODS = "POST, GET, OPTIONS, DELETE, PUT, PATCH";
    private static final String DEFAULT_MAX_AGE = "3600";
    private static final String DEFAULT_ALLOW_HEADERS = "x-requested-with, authorization, "
                                                      + "content-type, x-auth-token";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        final HttpServletResponse response = (HttpServletResponse) res;
        String allowOrigin = System.getenv(SystemEnvCors.CORS_ALLOW_ORIGIN.toString());
        allowOrigin = allowOrigin == null ? DEFAULT_ALLOW_ORIGIN : allowOrigin;
        String allowMethods = System.getenv(SystemEnvCors.CORS_ALLOW_METHODS.toString());
        allowMethods = allowMethods == null ? DEFAULT_ALLOW_METHODS : allowMethods;
        String allowHeaders = System.getenv(SystemEnvCors.CORS_ALLOW_HEADERS.toString());
        allowHeaders = allowHeaders == null ? DEFAULT_ALLOW_HEADERS : allowHeaders;
        String maxAge = System.getenv(SystemEnvCors.CORS_MAX_AGE.toString());
        maxAge = maxAge == null ? DEFAULT_MAX_AGE : maxAge;

        response.setHeader("Access-Control-Allow-Origin", allowOrigin);
        response.setHeader("Access-Control-Allow-Methods", allowMethods);
        response.setHeader("Access-Control-Max-Age", maxAge);
        response.setHeader("Access-Control-Allow-Headers", allowHeaders);
        try {
            chain.doFilter(req, res);
        } catch (Exception e) {
            LOGGER.debug(e);
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

}
