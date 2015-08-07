package io.galeb;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.jms.annotation.EnableJms;

import io.galeb.entity.Environment;
import io.galeb.entity.Farm;
import io.galeb.entity.Project;
import io.galeb.entity.Rule;
import io.galeb.entity.RuleType;
import io.galeb.entity.Target;
import io.galeb.entity.TargetType;
import io.galeb.entity.VirtualHost;

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
        repositoryRestConfiguration.setReturnBodyOnCreate(true)
                                   .setReturnBodyOnUpdate(true)
                                   .exposeIdsFor(Environment.class,
                                                 Farm.class,
                                                 Project.class,
                                                 Rule.class,
                                                 RuleType.class,
                                                 Target.class,
                                                 TargetType.class,
                                                 VirtualHost.class);
    }

}
