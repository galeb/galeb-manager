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

package io.galeb.manager;

import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setShowBanner(false);
        app.setDefaultProperties(getDefaultProperties());
        app.run(args);
    }

    private static Properties getDefaultProperties() {
        String testWhileIdle = System.getenv("GALEB_DB_TEST_WHILE_IDLE");
        String ddlAuto = System.getenv("GALEB_DB_DDL_AUTO");
        String dialect = System.getenv("GALEB_DB_DIALECT");
        String showSql = System.getenv("GALEB_DB_SHOWSQL");
        String namingStrategy = System.getenv("GALEB_DB_NAMING_STRATEGY");

        Properties defaultProperties = new Properties();
        defaultProperties.put("spring.datasource.testWhileIdle",
                testWhileIdle != null ? Boolean.parseBoolean(testWhileIdle) : true);
        defaultProperties.put("spring.jpa.hibernate.ddl-auto",
                ddlAuto != null ? ddlAuto : "validate");
        defaultProperties.put("spring.jpa.hibernate.hbm2ddl.auto",
                ddlAuto != null ? ddlAuto : "validate");
        defaultProperties.put("spring.jpa.database-platform",
                dialect != null ? dialect : "org.hibernate.dialect.H2Dialect");
        defaultProperties.put("spring.jpa.properties.hibernate.dialect",
                dialect != null ? dialect : "org.hibernate.dialect.H2Dialect");
        defaultProperties.put("spring.jpa.show-sql",
                showSql != null ? Boolean.parseBoolean(showSql) : false);
        defaultProperties.put("spring.jpa.hibernate.naming-strategy",
                namingStrategy != null ? namingStrategy : "org.hibernate.cfg.ImprovedNamingStrategy");

        return defaultProperties;
    }

}
