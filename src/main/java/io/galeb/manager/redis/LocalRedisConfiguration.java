package io.galeb.manager.redis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class LocalRedisConfiguration {

    private static final Log LOGGER = LogFactory.getLog(LocalRedisConfiguration.class);

    @Autowired
    private Environment env;

    @Bean
    public JedisConnectionFactory connectionFactory() {
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

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setBlockWhenExhausted(true);

        RedisSentinelConfiguration sentinelConfig = null;
        if (useSentinel != null && !useSentinel.equals("false")) {
            try {
                Iterable<RedisNode> redisSentinelNodesList = redisSentinelNodesStringList.stream().map(node ->
                                new RedisNode(node.split(":")[0], Integer.parseInt(node.split(":")[1]))
                ).collect(Collectors.toList());
                sentinelConfig = new RedisSentinelConfiguration();
                sentinelConfig.master(masterName).setSentinels(redisSentinelNodesList);
                jedisConnectionFactory = new JedisConnectionFactory(sentinelConfig);
                jedisConnectionFactory.setPoolConfig(poolConfig);
            } catch (Exception e) {
                LOGGER.error(e);
            }
        } else {
            jedisConnectionFactory = new JedisConnectionFactory();
            jedisConnectionFactory.setPoolConfig(poolConfig);
            jedisConnectionFactory.setHostName(hostName);
            try {
                jedisConnectionFactory.setPort(Integer.parseInt(port));
            } catch (NumberFormatException e) {
                LOGGER.error(e);
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
            LOGGER.error(e);
        }
        return jedisConnectionFactory;
    }

}
