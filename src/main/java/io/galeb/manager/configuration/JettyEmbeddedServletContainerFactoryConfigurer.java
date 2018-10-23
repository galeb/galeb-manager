/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2018 Globo.com
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

package io.galeb.manager.configuration;

import java.util.Optional;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JettyEmbeddedServletContainerFactoryConfigurer {

    private static final int JETTY_MIN_THREADS = Integer.parseInt(
        Optional.ofNullable(System.getenv("JETTY_MIN_THREADS")).orElse("8"));
    private static final int JETTY_MAX_THREADS = Integer.parseInt(
        Optional.ofNullable(System.getenv("JETTY_MAX_THREADS")).orElse("200"));
    private static final int JETTY_IDLE_TIMEOUT = Integer.parseInt(
        Optional.ofNullable(System.getenv("JETTY_IDLE_TIMEOUT")).orElse("60000"));

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory() {
        final JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory(serverPort);
        factory.addServerCustomizers((Server server) -> {
            final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
            threadPool.setMinThreads(JETTY_MIN_THREADS);
            threadPool.setMaxThreads(JETTY_MAX_THREADS);
            threadPool.setIdleTimeout(JETTY_IDLE_TIMEOUT);
        });
        return factory;
    }

}
