package io.galeb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.galeb.handler.ProjectHandler;
import io.galeb.handler.TypeHandler;

@Configuration
public class RepositoryConfiguration {

    @Bean
    public ProjectHandler projectHandler() {
        return new ProjectHandler();
    }

    @Bean
    public TypeHandler typeHandler() {
        return new TypeHandler();
    }

}
