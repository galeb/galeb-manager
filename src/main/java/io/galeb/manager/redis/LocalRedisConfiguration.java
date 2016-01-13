package io.galeb.manager.redis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class LocalRedisConfiguration {

    private static final Log LOGGER = LogFactory.getLog(LocalRedisConfiguration.class);

    @Autowired
    private Environment env;

    @Bean
    public RedisConnectionFactory connectionFactory() {
        String hostName = System.getenv("REDIS_HOSTNAME");
        hostName = hostName == null ?
                env.getProperty("spring.redis.host", "127.0.0.1") : hostName;
        String port = System.getenv("REDIS_PORT");
        port = port == null ?
                env.getProperty("spring.redis.port", "6379") : port;
        String password = System.getenv("REDIS_PASSWORD");
        password = password == null ?
                env.getProperty("spring.redis.password", "") : password;
        String database = System.getenv("REDIS_DATABASE");
        database = database == null ?
                env.getProperty("spring.redis.database", "0") : database;
        String connectionTimeout = System.getenv("REDIS_TIMEOUT");
        connectionTimeout = connectionTimeout == null ?
                env.getProperty("spring.redis.timeout", "") : connectionTimeout;

        String useSentinel = System.getenv("REDIS_USE_SENTINEL");

        String masterName = System.getenv("REDIS_SENTINEL_MASTER_NAME");
        if (masterName != null && System.getProperty("spring.redis.sentinel.master") == null) {
            System.setProperty("spring.redis.sentinel.master", masterName);
        }

        String redisSentinelNodes = System.getenv("REDIS_SENTINEL_NODES");
        if (redisSentinelNodes != null && System.getProperty("spring.redis.sentinel.nodes") == null) {
            System.setProperty("spring.redis.sentinel.nodes", redisSentinelNodes);
        }

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();

        if (useSentinel == null || useSentinel.equals("false")) {
            connectionFactory.setHostName(hostName);
            try {
                connectionFactory.setPort(Integer.parseInt(port));
            } catch (NumberFormatException e) {
                LOGGER.error(e);
            }
        }
        try {
            if (!"".equals(password)) {
                connectionFactory.setPassword(password);
            }
            if (!"".equals(database)) {
                connectionFactory.setDatabase(Integer.parseInt(database));
            }
            if (!"".equals(connectionTimeout)) {
                connectionFactory.setTimeout(Integer.parseInt(connectionTimeout));
            }
        } catch (NumberFormatException e) {
            LOGGER.error(e);
        }
        return connectionFactory;
    }

}
