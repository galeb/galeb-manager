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

import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.engine.driver.impl.GalebV32Driver;
import io.galeb.manager.engine.listeners.VirtualHostEngine;
import io.galeb.manager.engine.util.VirtualHostAliasBuilder;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Project;
import io.galeb.manager.entity.VirtualHost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VirtualhostFactory extends AbstractFactory<VirtualHost> {

    private static final Log LOGGER = LogFactory.getLog(VirtualhostFactory.class);
    private final VirtualHostAliasBuilder virtualHostAliasBuilder = new VirtualHostAliasBuilder();

    @Override
    public VirtualHost build(String name) {
        Environment environment = new Environment("NULL");
        Project project = new Project("NULL");
        return new VirtualHost(name, environment, project);
    }

    public VirtualHostEngine buildVirtualHostEngine(Driver driver) {
        return (VirtualHostEngine) new VirtualHostEngine()
                                        .setVirtualHostAliasBuilder(virtualHostAliasBuilder)
                                        .setDriver(driver)
                                        .setFarmRepository(new FarmFactory().mockFarmRepository());

    }
}
