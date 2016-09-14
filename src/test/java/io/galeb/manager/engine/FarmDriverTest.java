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

package io.galeb.manager.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.engine.driver.impl.GalebV32Driver;
import io.galeb.manager.engine.listeners.FarmEngine;
import io.galeb.manager.engine.listeners.VirtualHostEngine;
import io.galeb.manager.engine.util.VirtualHostAliasBuilder;
import io.galeb.manager.entity.*;
import io.galeb.manager.httpclient.FakeFarmClient;
import io.galeb.manager.test.factory.FarmFactory;
import io.galeb.manager.test.factory.VirtualhostFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.*;

public class FarmDriverTest {

    private static final Log LOGGER = LogFactory.getLog(GalebV32Driver.class);

    private final FarmFactory farmFactory = new FarmFactory();
    private final VirtualhostFactory virtualhostFactory = new VirtualhostFactory();
    private final FakeFarmClient fakeFarmClient = new FakeFarmClient();
    private final Driver driver = DriverBuilder.build(GalebV32Driver.DRIVER_NAME).addResource(fakeFarmClient);
    private final FarmEngine farmEngine = (FarmEngine) new FarmEngine().setDriver(driver);
    private final VirtualHostAliasBuilder virtualHostAliasBuilder = new VirtualHostAliasBuilder();
    private final VirtualHostEngine virtuahostEngine = (VirtualHostEngine) new VirtualHostEngine()
                                                                                .setVirtualHostAliasBuilder(virtualHostAliasBuilder)
                                                                                .setDriver(driver);

    private void logTestedMethod() {
        LOGGER.info("Testing " + this.getClass().getSimpleName() + "." +
                    Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Before
    public void setUp() {
        fakeFarmClient.deleteAll();
    }

    @Test
    public void notExist() {
        logTestedMethod();
        Properties properties = new Properties();
        boolean result = driver.exist(properties);
        Assert.isTrue(!result);
    }

    @Test
    public void getFarmAlwaysReturnOK() throws JsonProcessingException {
        logTestedMethod();

        Farm farm = farmFactory.build("api");
        boolean result = driver.exist(farmEngine.getPropertiesWithEntities(farm, farm.getApi(), Collections.emptyMap()));
        Assert.isTrue(result);
    }

    @Test
    public void removeFarmAlwaysReturnAccept() throws JsonProcessingException {
        logTestedMethod();

        Farm farm = farmFactory.build("api");
        boolean result = driver.remove(farmEngine.getPropertiesWithEntities(farm, farm.getApi(), Collections.emptyMap()));
        Assert.isTrue(result);
    }

    @Test
    public void createAndUpdateFarmIsNotPossible() {
        logTestedMethod();

        Farm farm = farmFactory.build("api");
        boolean resultCreate = driver.create(farmEngine.getPropertiesWithEntities(farm, farm.getApi(), Collections.emptyMap()));
        boolean resultUpdate = driver.update(farmEngine.getPropertiesWithEntities(farm, farm.getApi(), Collections.emptyMap()));

        Assert.isTrue(!resultCreate, "Create Farm is possible?");
        Assert.isTrue(!resultUpdate, "Update Farm is possible?");
    }

    @Test
    public void diffEmpty() throws Exception {
        logTestedMethod();

        final Map<String, List<?>> entitiesMap = farmFactory.entitiesMap();
        Farm farm = farmFactory.build("api");
        Properties properties = farmEngine.getPropertiesWithEntities(farm, farm.getApi(), entitiesMap);

        Map<String, Map<String, Object>> diff = new HashMap<>();
        Map<String, Map<String, Map<String, String>>> remoteMultiMap = driver.getAll(properties);
        diff.putAll(driver.diff(properties, remoteMultiMap));
        int diffSize = diff.size();

        Assert.isTrue(diffSize == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void diffNotEmpty() throws Exception {
        logTestedMethod();

        final Map<String, List<?>> entitiesMap = farmFactory.entitiesMap();
        VirtualHost virtualhost = virtualhostFactory.build(UUID.randomUUID().toString());
        List<VirtualHost> virtualhosts = (List<VirtualHost>) entitiesMap.get(VirtualHost.class.getSimpleName().toLowerCase());
        virtualhosts.add(virtualhost);
        Farm farm = farmFactory.build("api");
        Properties farmProperties = farmEngine.getPropertiesWithEntities(farm, farm.getApi(), entitiesMap);
        virtuahostEngine.create(virtualhost, virtualhostFactory.jmsHeaderProperties());

        final Map<String, Map<String, Map<String, String>>> remoteMultiMap = driver.getAll(farmProperties);
        final Map<String, Map<String, Object>> diffMap = driver.diff(farmProperties, remoteMultiMap);
        int diffSize = diffMap.size();

        Assert.isTrue(diffSize == 0);
    }

}
