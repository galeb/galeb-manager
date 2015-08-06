package io.galeb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.galeb.handler.EnvironmentHandler;
import io.galeb.handler.FarmHandler;
import io.galeb.handler.ProjectHandler;
import io.galeb.handler.ProviderHandler;
import io.galeb.handler.RuleHandler;
import io.galeb.handler.RuleTypeHandler;
import io.galeb.handler.TargetHandler;
import io.galeb.handler.TargetTypeHandler;
import io.galeb.handler.VirtualHostHandler;

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

    @Bean
    public TargetTypeHandler targetTypeHandler() {
        return new TargetTypeHandler();
    }

    @Bean
    public FarmHandler farmHandler() {
        return new FarmHandler();
    }

    @Bean
    public VirtualHostHandler virtualHostHandler() {
        return new VirtualHostHandler();
    }

    @Bean
    public TargetHandler targetHandler() {
        return new TargetHandler();
    }

    @Bean
    public RuleHandler ruleHandler() {
        return new RuleHandler();
    }

    @Bean
    public ProviderHandler providerHandler() {
        return new ProviderHandler();
    }

}
