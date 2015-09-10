/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2015 Globo.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.galeb.manager.repository;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.galeb.manager.entity.Account;
import io.galeb.manager.entity.BalancePolicy;
import io.galeb.manager.entity.BalancePolicyType;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Project;
import io.galeb.manager.entity.Provider;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.RuleType;
import io.galeb.manager.entity.Target;
import io.galeb.manager.entity.TargetType;
import io.galeb.manager.entity.Team;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.handler.AccountHandler;
import io.galeb.manager.handler.BalancePolicyHandler;
import io.galeb.manager.handler.BalancePolicyTypeHandler;
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
@Import(RepositoryRestMvcConfiguration.class)
@EnableTransactionManagement
public class RepositoryConfiguration {

    public static final String API_VERSION = "1";

    @Autowired
    private RepositoryRestConfiguration repositoryRestConfiguration;

    @PostConstruct
    public void postConstruct() {
        repositoryRestConfiguration.exposeIdsFor(Environment.class,
                                                 Farm.class,
                                                 Project.class,
                                                 Rule.class,
                                                 RuleType.class,
                                                 Target.class,
                                                 TargetType.class,
                                                 VirtualHost.class,
                                                 Account.class,
                                                 Provider.class,
                                                 Team.class,
                                                 BalancePolicyType.class,
                                                 BalancePolicy.class);

    }

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

    @Bean
    public BalancePolicyTypeHandler balancePolicyTypeHandler() {
        return new BalancePolicyTypeHandler();
    }

    @Bean
    public BalancePolicyHandler balancePolicyHandler() {
        return new BalancePolicyHandler();
    }

}
