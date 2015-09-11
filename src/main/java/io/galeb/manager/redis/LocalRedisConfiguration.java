package io.galeb.manager.redis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

@Configuration
public class LocalRedisConfiguration {

    private static final Log LOGGER = LogFactory.getLog(LocalRedisConfiguration.class);

    @Autowired
    private Environment env;

    @Bean
    public JedisConnectionFactory connectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
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

        jedisConnectionFactory.setHostName(hostName);
        try {
            jedisConnectionFactory.setPort(Integer.parseInt(port));
            if (!"".equals(password)) {
                jedisConnectionFactory.setPassword(password);
            }
            if (!"".equals(database)) {
                jedisConnectionFactory.setDatabase(Integer.parseInt(database));
            }
            if (!"".equals(connectionTimeout)) {
                jedisConnectionFactory.setTimeout(Integer.parseInt(connectionTimeout));
            }
        } catch (NumberFormatException e) {
            LOGGER.error(e);
        }
        return jedisConnectionFactory;
    }

}
