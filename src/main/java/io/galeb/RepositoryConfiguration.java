package io.galeb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.galeb.handler.EnvironmentHandler;
import io.galeb.handler.ProjectHandler;
import io.galeb.handler.RuleTypeHandler;

@Configuration
public class RepositoryConfiguration {

    @Bean
    public ProjectHandler projectHandler() {
        return new ProjectHandler();
    }

    @Bean
    public EnvironmentHandler environmentHandler() {
        return new EnvironmentHandler();
    }

    @Bean
    public RuleTypeHandler ruleTypeHandler() {
        return new RuleTypeHandler();
    }

}
