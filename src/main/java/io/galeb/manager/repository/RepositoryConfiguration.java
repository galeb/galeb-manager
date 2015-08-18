package io.galeb.manager.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.galeb.manager.handler.AccountHandler;
import io.galeb.manager.handler.EnvironmentHandler;
import io.galeb.manager.handler.FarmHandler;
import io.galeb.manager.handler.ProjectHandler;
import io.galeb.manager.handler.ProviderHandler;
import io.galeb.manager.handler.RuleHandler;
import io.galeb.manager.handler.RuleTypeHandler;
import io.galeb.manager.handler.TargetHandler;
import io.galeb.manager.handler.TargetTypeHandler;
import io.galeb.manager.handler.TeamHandler;
import io.galeb.manager.handler.VirtualHostHandler;

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

    @Bean
    public AccountHandler accountHandler() {
        return new AccountHandler();
    }

    @Bean
    public TeamHandler teamHandler() {
        return new TeamHandler();
    }

}
