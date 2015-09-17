package io.galeb.manager.jms;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@Configuration
@EnableJms
public class JmsConfiguration {

    public static final String DISABLE_JMS = "DISABLE_JMS";

    @Bean
    public DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory() {
        return new DefaultJmsListenerContainerFactory();
    }

}
