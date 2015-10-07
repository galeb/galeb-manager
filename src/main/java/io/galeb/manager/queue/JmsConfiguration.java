package io.galeb.manager.queue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@Configuration
@EnableJms
public class JmsConfiguration {

    public static final String DISABLE_QUEUE = "DISABLE_QUEUE";

    @Bean
    public DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory() {
        return new DefaultJmsListenerContainerFactory();
    }

}
