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

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.engine.driver.impl.GalebV32Driver;
import io.galeb.manager.engine.listeners.AbstractEngine;
import io.galeb.manager.engine.listeners.VirtualHostEngine;
import io.galeb.manager.engine.util.VirtualHostAliasBuilder;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.httpclient.FakeFarmClient;
import io.galeb.manager.test.factory.VirtualhostFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.UUID;

public class VirtualhostDriverTest {

    private static final Log LOGGER = LogFactory.getLog(GalebV32Driver.class);

    private final VirtualhostFactory virtualhostFactory = new VirtualhostFactory();
    private final FakeFarmClient fakeFarmClient = new FakeFarmClient();
    private final Driver driver = DriverBuilder.build(GalebV32Driver.DRIVER_NAME).addResource(fakeFarmClient);
    private final VirtualHostAliasBuilder virtualHostAliasBuilder = new VirtualHostAliasBuilder();
    private final AbstractEngine<VirtualHost> virtuahostEngine = new VirtualHostEngine().setVirtualHostAliasBuilder(virtualHostAliasBuilder).setDriver(driver);

    private void logTestedMethod() {
        LOGGER.info("Testing " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Before
    public void setUp() {
        fakeFarmClient.deleteAll();
    }

    @Test
    public void virtualhostNotExist() {
        logTestedMethod();
        boolean result = driver.exist(virtualhostFactory.makeProperties(virtualhostFactory.build(UUID.randomUUID().toString())));

        Assert.isTrue(!result);
    }

    @Test
    public void createVirtualhost() {
        logTestedMethod();
        VirtualHost virtualHost = virtualhostFactory.build(UUID.randomUUID().toString());
        virtuahostEngine.create(virtualHost, virtualhostFactory.jmsHeaderProperties());
        boolean resultExist = driver.exist(virtualhostFactory.makeProperties(virtualHost));

        Assert.isTrue(resultExist);
    }

    @Test
    public void updateVirtualhost() throws Exception {
        logTestedMethod();
        VirtualHost virtualHost = virtualhostFactory.build(UUID.randomUUID().toString());
        Properties properties = virtualhostFactory.makeProperties(virtualHost);
        String api = properties.getOrDefault("api", "UNDEF").toString();
        String virtualHostName = virtualHost.getName();
        virtuahostEngine.create(virtualHost, virtualhostFactory.jmsHeaderProperties());
        Map<String, String> map = driver.getAll(properties).get("virtualhost").get(api + "/virtualhost/" + virtualHostName + "@");
        String versionStr = map.get("version");
        int versionOrig = Integer.parseInt(versionStr);
        boolean resultExist = driver.exist(properties);
        virtualHost.updateHash();
        virtuahostEngine.update(virtualHost, virtualhostFactory.jmsHeaderProperties());
        map = driver.getAll(properties).get("virtualhost").get(api + "/virtualhost/" + virtualHostName + "@");
        versionStr = map.get("version");
        int versionNew = Integer.parseInt(versionStr);

        Assert.isTrue(resultExist && versionNew > versionOrig);
    }

    @Test
    public void removeVirtualhost() {
        logTestedMethod();
        VirtualHost virtualHost = virtualhostFactory.build(UUID.randomUUID().toString());
        Properties properties = virtualhostFactory.makeProperties(virtualHost);
        virtuahostEngine.create(virtualHost, virtualhostFactory.jmsHeaderProperties());
        boolean resultExist = driver.exist(properties);
        virtuahostEngine.remove(virtualHost, virtualhostFactory.jmsHeaderProperties());
        boolean resultNotExist = !driver.exist(properties);

        Assert.isTrue(resultExist && resultNotExist);
    }

    @Test
    public void createAliasesWithNewVirtualhost() {
        logTestedMethod();
        VirtualHost virtualHost = virtualhostFactory.build(UUID.randomUUID().toString());
        String anAlias = UUID.randomUUID().toString();
        String otherAlias = UUID.randomUUID().toString();
        VirtualHost anAliasVirtualHost = virtualhostFactory.build(anAlias);
        VirtualHost otherAliasVirtualHost = virtualhostFactory.build(otherAlias);
        virtualHost.getAliases().add(anAlias);
        virtualHost.getAliases().add(otherAlias);

        virtuahostEngine.create(virtualHost, virtualhostFactory.jmsHeaderProperties());

        boolean resultExist = driver.exist(virtualhostFactory.makeProperties(virtualHost));
        boolean resultExistAnAlias = driver.exist(virtualhostFactory.makeProperties(anAliasVirtualHost));
        boolean resultExistOtherAlias = driver.exist(virtualhostFactory.makeProperties(otherAliasVirtualHost));

        Assert.isTrue(resultExist && resultExistAnAlias && resultExistOtherAlias);
    }

    @Test
    public void createAliasesWithSavedVirtualhost() {
        logTestedMethod();
        VirtualHost virtualHost = virtualhostFactory.build(UUID.randomUUID().toString());
        String anAlias = UUID.randomUUID().toString();
        String otherAlias = UUID.randomUUID().toString();
        VirtualHost anAliasVirtualHost = virtualhostFactory.build(anAlias);
        VirtualHost otherAliasVirtualHost = virtualhostFactory.build(otherAlias);
        virtualHost.getAliases().add(anAlias);

        virtuahostEngine.create(virtualHost, virtualhostFactory.jmsHeaderProperties());

        virtualHost.getAliases().add(otherAlias);

        virtuahostEngine.update(virtualHost, virtualhostFactory.jmsHeaderProperties());

        boolean resultExist = driver.exist(virtualhostFactory.makeProperties(virtualHost));
        boolean resultExistAnAlias = driver.exist(virtualhostFactory.makeProperties(anAliasVirtualHost));
        boolean resultExistOtherAlias = driver.exist(virtualhostFactory.makeProperties(otherAliasVirtualHost));

        Assert.isTrue(resultExist && resultExistAnAlias && resultExistOtherAlias);
    }

}
