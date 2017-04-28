package io.galeb.manager.queue;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.connection.CachingConnectionFactory;

import javax.jms.JMSException;

@Configuration
@EnableJms
public class JmsConfiguration {

    private static final String BROKEN_ACCEPTOR = System.getProperty("QUEUE_CONN", "tcp://localhost:61616?protocols=Core");

    public static final String DISABLE_QUEUE = "DISABLE_QUEUE";

    @Bean(name="connectionFactory")
    public CachingConnectionFactory cachingConnectionFactory() throws JMSException {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKEN_ACCEPTOR);
        connectionFactory.setUser("guest");
        connectionFactory.setPassword("guest");
        cachingConnectionFactory.setTargetConnectionFactory(connectionFactory);
        cachingConnectionFactory.setSessionCacheSize(100);
        cachingConnectionFactory.setCacheConsumers(true);
        cachingConnectionFactory.createConnection().start();
        return cachingConnectionFactory;
    }

}
