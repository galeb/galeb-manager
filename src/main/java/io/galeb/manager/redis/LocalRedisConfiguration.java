package io.galeb.manager.redis;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class LocalRedisConfiguration {

    private static final Log LOGGER = LogFactory.getLog(LocalRedisConfiguration.class);

    public static final String REDIS_MAXIDLE  = System.getProperty("REDIS_MAXIDLE", "100");
    public static final String REDIS_TIMEOUT  = System.getProperty("REDIS_TIMEOUT", "60000");
    public static final String REDIS_MAXTOTAL = System.getProperty("REDIS_MAXTOTAL", "128");

    @Autowired
    private Environment env;

    @Bean(name = "redisTemplate")
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

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

        JedisConnectionFactory jedisConnectionFactory = null;

        String useSentinel = System.getenv("REDIS_USE_SENTINEL");

        String masterName = System.getenv("REDIS_SENTINEL_MASTER_NAME");
        masterName = masterName == null ?
                env.getProperty("spring.redis.sentinel.master", "mymaster") : masterName;

        String redisSentinelNodes = System.getenv("REDIS_SENTINEL_NODES");
        redisSentinelNodes = redisSentinelNodes == null ?
                env.getProperty("spring.redis.sentinel.nodes", "127.0.0.1:26379") : redisSentinelNodes;
        List<String> redisSentinelNodesStringList = Arrays.asList(redisSentinelNodes.split(","));

        RedisSentinelConfiguration sentinelConfig = null;
        if (useSentinel != null && !useSentinel.equals("false")) {
            try {
                Iterable<RedisNode> redisSentinelNodesList = redisSentinelNodesStringList.stream().map(node ->
                                new RedisNode(node.split(":")[0], Integer.parseInt(node.split(":")[1]))
                ).collect(Collectors.toList());
                sentinelConfig = new RedisSentinelConfiguration();
                sentinelConfig.master(masterName).setSentinels(redisSentinelNodesList);
                jedisConnectionFactory = new JedisConnectionFactory(sentinelConfig);
                jedisConnConfig(jedisConnectionFactory);

            } catch (Exception e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        } else {
            jedisConnectionFactory = new JedisConnectionFactory();
            jedisConnectionFactory.setHostName(hostName);
            jedisConnConfig(jedisConnectionFactory);
            try {
                jedisConnectionFactory.setPort(Integer.parseInt(port));
            } catch (NumberFormatException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
        try {
            if (jedisConnectionFactory != null) {
                if (!"".equals(password)) {
                    jedisConnectionFactory.setPassword(password);
                }
                if (!"".equals(database)) {
                    jedisConnectionFactory.setDatabase(Integer.parseInt(database));
                }
                if (!"".equals(connectionTimeout)) {
                    jedisConnectionFactory.setTimeout(Integer.parseInt(connectionTimeout));
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return jedisConnectionFactory;
    }

    private void jedisConnConfig(final JedisConnectionFactory jedisConnectionFactory) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        try {
            poolConfig.setMaxTotal(Integer.parseInt(REDIS_MAXTOTAL));
            poolConfig.setMaxIdle(Integer.parseInt(REDIS_MAXIDLE));
            poolConfig.setBlockWhenExhausted(true);

            jedisConnectionFactory.setPoolConfig(poolConfig);
            jedisConnectionFactory.setUsePool(true);
            jedisConnectionFactory.setTimeout(Integer.parseInt(REDIS_TIMEOUT));
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }

}
