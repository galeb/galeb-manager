package io.galeb.manager.security.config;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.ExpiringSession;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;

@Configuration
@EnableRedisHttpSession
public class SessionRepositoryConfig {

    private static final Log LOGGER = LogFactory.getLog(SessionRepositoryConfig.class);

    private static final String SERVER_SESSION_TIMEOUT_PROP = "server.session-timeout";
    private static final String SERVER_SESSION_TIMEOUT_DEF  = "1800";

    @Value("#{systemProperties['server.session-timeout']?:1800}")
    private int maxInactiveIntervalInSeconds;

    @Bean
    public HttpSessionStrategy httpSessionStrategy() {
        return new HeaderHttpSessionStrategy();
    }

    @Primary
    @Bean
    RedisOperationsSessionRepository sessionRepository(RedisTemplate<String, ExpiringSession> sessionRedisTemplate) {
        final RedisOperationsSessionRepository sessionRepository = new RedisOperationsSessionRepository(sessionRedisTemplate);
        String propertyServerSession = System.getProperty(SERVER_SESSION_TIMEOUT_PROP, SERVER_SESSION_TIMEOUT_DEF);
        try {
            if (!propertyServerSession.equals(SERVER_SESSION_TIMEOUT_DEF)) {
                if (maxInactiveIntervalInSeconds == Integer.parseInt(SERVER_SESSION_TIMEOUT_DEF)) {
                    maxInactiveIntervalInSeconds = Integer.parseInt(propertyServerSession);
                }
                sessionRepository.setDefaultMaxInactiveInterval(maxInactiveIntervalInSeconds);
            }
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return sessionRepository;
    }

}
