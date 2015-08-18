package io.galeb.manager;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.jms.annotation.EnableJms;

import io.galeb.manager.entity.Account;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Project;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.RuleType;
import io.galeb.manager.entity.Target;
import io.galeb.manager.entity.TargetType;
import io.galeb.manager.entity.Team;
import io.galeb.manager.entity.VirtualHost;

@SpringBootApplication
@EnableJms
@Import(RepositoryRestMvcConfiguration.class)
public class Application {

    public static final String API_VERSION = "2";

    @Autowired
    private RepositoryRestConfiguration repositoryRestConfiguration;

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

    }

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
                                                 Team.class);
    }

}
