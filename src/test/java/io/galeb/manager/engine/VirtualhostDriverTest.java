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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VirtualhostDriverTest {

    private static final Log LOGGER = LogFactory.getLog(GalebV32Driver.class);

    private final VirtualhostFactory virtualhostFactory = new VirtualhostFactory();
    private final FakeFarmClient fakeFarmClient = new FakeFarmClient();
    private final Driver driver = DriverBuilder.build(GalebV32Driver.DRIVER_NAME).addResource(fakeFarmClient);
    private final VirtualHostAliasBuilder virtualHostAliasBuilder = new VirtualHostAliasBuilder();
    private final AbstractEngine<VirtualHost> virtuahostEngine = new VirtualHostEngine()
                                                                        .setVirtualHostAliasBuilder(virtualHostAliasBuilder)
                                                                        .setDriver(driver);
    private final Map<String, String> jmsHeaders = virtualhostFactory.jmsHeaderProperties();

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

        // given
        String virtualhostName = UUID.randomUUID().toString();
        Properties properties = virtualhostFactory.makeProperties(virtualhostFactory.build(virtualhostName));

        // when
        boolean result = driver.exist(properties);

        // then
        Assert.isTrue(!result);
    }

    @Test
    public void createVirtualhost() {
        logTestedMethod();

        // given
        String virtualhostName = UUID.randomUUID().toString();
        VirtualHost virtualHost = virtualhostFactory.build(virtualhostName);
        Properties properties = virtualhostFactory.makeProperties(virtualHost);

        // when
        virtuahostEngine.create(virtualHost, jmsHeaders);
        boolean resultExist = driver.exist(properties);

        // then
        Assert.isTrue(resultExist);
    }

    @Test
    public void updateVirtualhost() throws Exception {
        logTestedMethod();

        // given
        String virtualhostName = UUID.randomUUID().toString();
        VirtualHost virtualHost = virtualhostFactory.build(virtualhostName);
        Properties properties = virtualhostFactory.makeProperties(virtualHost);
        String api = properties.getOrDefault("api", "UNDEF").toString();
        String virtualHostName = virtualHost.getName();

        // when
        virtuahostEngine.create(virtualHost, jmsHeaders);
        String multiMapKey = api + "/virtualhost/" + virtualHostName + "@";
        Map<String, String> map = driver.getAll(properties).get("virtualhost").get(multiMapKey);
        String versionStr = map.get("version");
        int versionOrig = Integer.parseInt(versionStr);
        boolean resultExist = driver.exist(properties);
        virtualHost.updateHash();
        virtuahostEngine.update(virtualHost, jmsHeaders);
        map = driver.getAll(properties).get("virtualhost").get(multiMapKey);
        versionStr = map.get("version");
        int versionNew = Integer.parseInt(versionStr);

        // then
        boolean isNewVersionGreaterThenOldVersion = versionNew > versionOrig;
        Assert.isTrue(resultExist && isNewVersionGreaterThenOldVersion);
    }

    @Test
    public void removeVirtualhost() {
        logTestedMethod();

        // given
        String virtualhostName = UUID.randomUUID().toString();
        VirtualHost virtualHost = virtualhostFactory.build(virtualhostName);
        Properties properties = virtualhostFactory.makeProperties(virtualHost);

        // when
        virtuahostEngine.create(virtualHost, jmsHeaders);
        boolean resultExist = driver.exist(properties);
        virtuahostEngine.remove(virtualHost, jmsHeaders);
        boolean resultNotExist = !driver.exist(properties);

        // then
        Assert.isTrue(resultExist && resultNotExist);
    }

    @Test
    public void createAliasesWithNewVirtualhost() {
        logTestedMethod();

        // given
        String virtualhostName = UUID.randomUUID().toString();
        String anAliasName = UUID.randomUUID().toString();
        String otherAliasName = UUID.randomUUID().toString();

        VirtualHost virtualHost = virtualhostFactory.build(virtualhostName);
        VirtualHost anAliasVirtualHost = virtualhostFactory.build(anAliasName);
        VirtualHost otherAliasVirtualHost = virtualhostFactory.build(otherAliasName);

        Properties virtualhostProperties = virtualhostFactory.makeProperties(virtualHost);
        Properties anAliasProperties = virtualhostFactory.makeProperties(anAliasVirtualHost);
        Properties otherAliasProperties = virtualhostFactory.makeProperties(otherAliasVirtualHost);

        // when
        virtualHost.getAliases().add(anAliasName); // add an alias
        virtualHost.getAliases().add(otherAliasName); // add an other alias
        virtuahostEngine.create(virtualHost, jmsHeaders); // create virtualhost with two alias

        boolean resultExistVirtualhost = driver.exist(virtualhostProperties);
        boolean resultExistAnAlias     = driver.exist(anAliasProperties);
        boolean resultExistOtherAlias  = driver.exist(otherAliasProperties);

        // then
        Assert.isTrue(resultExistVirtualhost && resultExistAnAlias && resultExistOtherAlias);
    }

    @Test
    public void createAliasesWithSavedVirtualhost() {
        logTestedMethod();

        // given
        String virtualhostName = UUID.randomUUID().toString();
        String anAliasName = UUID.randomUUID().toString();
        String otherAliasName = UUID.randomUUID().toString();

        VirtualHost virtualHost = virtualhostFactory.build(virtualhostName);
        VirtualHost anAliasVirtualHost = virtualhostFactory.build(anAliasName);
        VirtualHost otherAliasVirtualHost = virtualhostFactory.build(otherAliasName);

        Properties virtualhostProperties = virtualhostFactory.makeProperties(virtualHost);
        Properties anAliasProperties = virtualhostFactory.makeProperties(anAliasVirtualHost);
        Properties otherAliasProperties = virtualhostFactory.makeProperties(otherAliasVirtualHost);
        Set<String> aliases = new HashSet<>();

        // when
        aliases.add(anAliasName); // add only one alias
        virtualHost.setAliases(aliases);
        virtuahostEngine.create(virtualHost, jmsHeaders); // create virtualhost with one alias
        aliases.add(otherAliasName); // add an other alias
        virtualHost.setAliases(aliases);
        virtuahostEngine.update(virtualHost, jmsHeaders); // update virtualhost (now with two aliases)

        boolean resultExistVirtualhost = driver.exist(virtualhostProperties);
        boolean resultExistAnAlias     = driver.exist(anAliasProperties);
        boolean resultExistOtherAlias  = driver.exist(otherAliasProperties);

        // then
        Assert.isTrue(resultExistVirtualhost && resultExistAnAlias && resultExistOtherAlias);
    }

}
