package io.galeb.manager.repository;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfiguration {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {

        String driverEnvName = System.getProperty("io.galeb.manager.datasource.driver.env", "GALEB_DB_DRIVER");
        String driver = System.getenv(driverEnvName);
        driver = driver != null ? driver : "org.h2.Driver";

        String urlEnvName = System.getProperty("io.galeb.manager.datasource.url.env", "GALEB_DB_URL");
        String url = System.getenv(urlEnvName);
        url = url != null ? url : "jdbc:h2:mem:galeb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

        String usernameEnvName = System.getProperty("io.galeb.manager.datasource.username.env", "GALEB_DB_USER");
        String username = System.getenv(usernameEnvName);
        username = username != null ? username : "root";

        String passwordEnvName = System.getProperty("io.galeb.manager.datasource.password.env", "GALEB_DB_PASS");
        String password = System.getenv(passwordEnvName);
        password = password != null ? password : "";

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name", driver));
        dataSource.setJdbcUrl(env.getProperty("spring.datasource.url", url));
        dataSource.setUsername(env.getProperty("spring.datasource.username", username));
        dataSource.setPassword(env.getProperty("spring.datasource.password", password));
        dataSource.setConnectionTimeout(60000);

        return dataSource;
    }

}
