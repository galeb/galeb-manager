/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2016 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.manager.test.factory;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.listeners.VirtualHostEngine;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Project;
import io.galeb.manager.entity.VirtualHost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class VirtualhostFactory {

    private static final Log LOGGER = LogFactory.getLog(VirtualhostFactory.class);

    public VirtualHost build(String name) {
        Environment environment = new Environment("NULL");
        Project project = new Project("NULL");
        return new VirtualHost(name, environment, project);
    }

    public Properties makeProperties(VirtualHost virtualHost) {
        return new VirtualHostEngine().makeProperties(virtualHost, jmsHeaderProperties());
    }

    public Map<String, String> jmsHeaderProperties() {
        Map<String, String> jmsHeaderProperties = new HashMap<>();
        jmsHeaderProperties.put("api", "api");
        return jmsHeaderProperties;
    }

}
