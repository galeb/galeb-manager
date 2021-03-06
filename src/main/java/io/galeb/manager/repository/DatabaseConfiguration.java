package io.galeb.manager.repository;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfiguration {

    private static String url = "jdbc:h2:mem:galeb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static String driver = "org.h2.Driver";
    private static String username = "root";
    private static String password = "";
    static {
        String driverEnvName = System.getProperty("io.galeb.manager.datasource.driver.env", "GALEB_DB_DRIVER");
        driver = System.getenv(driverEnvName);
        driver = driver != null ? driver : "org.h2.Driver";

        String urlEnvName = System.getProperty("io.galeb.manager.datasource.url.env", "GALEB_DB_URL");
        url = System.getenv(urlEnvName);
        url = url != null ? url : "jdbc:h2:mem:galeb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

        String usernameEnvName = System.getProperty("io.galeb.manager.datasource.username.env", "GALEB_DB_USER");
        username = System.getenv(usernameEnvName);
        username = username != null ? username : "root";

        String passwordEnvName = System.getProperty("io.galeb.manager.datasource.password.env", "GALEB_DB_PASS");
        password = System.getenv(passwordEnvName);
        password = password != null ? password : "";
    }

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name", getDriver()));
        dataSource.setJdbcUrl(env.getProperty("spring.datasource.url", getUrl()));
        dataSource.setUsername(env.getProperty("spring.datasource.username", getUsername()));
        dataSource.setPassword(env.getProperty("spring.datasource.password", getPassword()));
        dataSource.setConnectionTimeout(60000);
        dataSource.setConnectionTestQuery("SELECT 1");

        return dataSource;
    }

    public static String getUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static String getDriver() {
        return driver;
    }
}
